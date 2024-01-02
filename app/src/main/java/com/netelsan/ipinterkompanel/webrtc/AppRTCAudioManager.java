/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.netelsan.ipinterkompanel.webrtc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;


import com.netelsan.ipinterkompanel.Constants;

import org.webrtc.ThreadUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static android.content.Context.AUDIO_SERVICE;

/**
 * AppRTCAudioManager manages all audio related parts of the AppRTC demo.
 */
public class AppRTCAudioManager {
    private static final String SPEAKERPHONE_AUTO = "auto";
    private static final String SPEAKERPHONE_TRUE = "true";
    private static final String SPEAKERPHONE_FALSE = "false";

    public static final int SELECTED_SOUND_CHANNEL = AudioManager.STREAM_VOICE_CALL;

    /**
     * AudioDevice is the names of possible audio devices that we currently
     * support.
     */
    public enum AudioDevice {SPEAKER_PHONE, WIRED_HEADSET, EARPIECE, BLUETOOTH, NONE}

    /**
     * AudioManager state.
     */
    public enum AudioManagerState {
        UNINITIALIZED,
        PREINITIALIZED,
        RUNNING,
    }

    /**
     * Selected audio device change event.
     */
    public interface AudioManagerEvents {
        // Callback fired once audio device is changed or list of available audio devices changed.
        void onAudioDeviceChanged(
                AudioDevice selectedAudioDevice, Set<AudioDevice> availableAudioDevices);
    }

    private final Context apprtcContext;
    @Nullable
    private AudioManager audioManager;

    @Nullable
    private AudioManagerEvents audioManagerEvents;
    private AudioManagerState amState;
    private int savedAudioMode = AudioManager.MODE_INVALID;
    private boolean savedIsSpeakerPhoneOn;
    private boolean savedIsMicrophoneMute;
    private boolean hasWiredHeadset;

    // Default audio device; speaker phone for video calls or earpiece for audio
    // only calls.
    private AudioDevice defaultAudioDevice;

    // Contains the currently selected audio device.
    // This device is changed automatically using a certain scheme where e.g.
    // a wired headset "wins" over speaker phone. It is also possible for a
    // user to explicitly select a device (and overrid any predefined scheme).
    // See |userSelectedAudioDevice| for details.
    private AudioDevice selectedAudioDevice;

    // Contains the user-selected audio device which overrides the predefined
    // selection scheme.
    // TODO(henrika): always set to AudioDevice.NONE today. Add support for
    // explicit selection based on choice by userSelectedAudioDevice.
    private AudioDevice userSelectedAudioDevice;

    // Contains speakerphone setting: auto, true or false
    private final String useSpeakerphone = SPEAKERPHONE_TRUE;

    // Contains a list of available audio devices. A Set collection is used to
    // avoid duplicate elements.
    private Set<AudioDevice> audioDevices = new HashSet<>();

    // Broadcast receiver for wired headset intent broadcasts.
//    private BroadcastReceiver wiredHeadsetReceiver;

    // Callback method for changes in audio focus.
    @Nullable
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;

    /**
     * This method is called when the proximity sensor reports a state change,
     * e.g. from "NEAR to FAR" or from "FAR to NEAR".
     */
    private void onProximitySensorChangedState() {
        if (!useSpeakerphone.equals(SPEAKERPHONE_AUTO)) {
            return;
        }

    }

    public static AppRTCAudioManager create(Context context) {
        return new AppRTCAudioManager(context);
    }

    private AppRTCAudioManager(Context context) {
        Log.d(Constants.LOG_TAG, "ctor");
        ThreadUtils.checkIsOnMainThread();
        apprtcContext = context;
        audioManager = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));

//        wiredHeadsetReceiver = new WiredHeadsetReceiver();
        amState = AudioManagerState.UNINITIALIZED;

        defaultAudioDevice = AudioDevice.SPEAKER_PHONE;

        Log.d(Constants.LOG_TAG, "defaultAudioDevice: " + defaultAudioDevice);
        logDeviceInfo();

        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        int volume_level = audioManager.getStreamVolume(AppRTCAudioManager.SELECTED_SOUND_CHANNEL);
        Log.d(Constants.LOG_TAG, "CallActivity volume_level=" + volume_level);
//        setDeviceVolume(context);
//        burada ses seviyesini ayarlar
    }

//    private void setDeviceVolume(Context context){
//        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
//        int volume_level = audioManager.getStreamVolume(AppRTCAudioManager.SELECTED_SOUND_CHANNEL);
//        int maxVolume = audioManager.getStreamMaxVolume(AppRTCAudioManager.SELECTED_SOUND_CHANNEL);
//    }

    @SuppressWarnings("deprecation")
    // TODO(henrika): audioManager.requestAudioFocus() is deprecated.
    public void start(AudioManagerEvents audioManagerEvents) {
        Log.d(Constants.LOG_TAG, "start");
        ThreadUtils.checkIsOnMainThread();
        if (amState == AudioManagerState.RUNNING) {
            Log.e(Constants.LOG_TAG, "AudioManager is already active");
            return;
        }
        // TODO(henrika): perhaps call new method called preInitAudio() here if UNINITIALIZED.

        Log.d(Constants.LOG_TAG, "AudioManager starts...");
        this.audioManagerEvents = audioManagerEvents;
        amState = AudioManagerState.RUNNING;

        // Store current audio state so we can restore it when stop() is called.
        savedAudioMode = audioManager.getMode();
        savedIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn();
        savedIsMicrophoneMute = audioManager.isMicrophoneMute();
        hasWiredHeadset = hasWiredHeadset();

        // Create an AudioManager.OnAudioFocusChangeListener instance.
        audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            // Called on the listener to notify if the audio focus for this listener has been changed.
            // The |focusChange| value indicates whether the focus was gained, whether the focus was lost,
            // and whether that loss is transient, or whether the new focus holder will hold it for an
            // unknown amount of time.
            // TODO(henrika): possibly extend support of handling audio-focus changes. Only contains
            // logging for now.
            @Override
            public void onAudioFocusChange(int focusChange) {
                final String typeOfChange;
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        typeOfChange = "AUDIOFOCUS_GAIN";
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT";
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
                        typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE";
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        typeOfChange = "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK";
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        typeOfChange = "AUDIOFOCUS_LOSS";
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        typeOfChange = "AUDIOFOCUS_LOSS_TRANSIENT";
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        typeOfChange = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                        break;
                    default:
                        typeOfChange = "AUDIOFOCUS_INVALID";
                        break;
                }
                Log.d(Constants.LOG_TAG, "onAudioFocusChange: " + typeOfChange);
            }
        };
//asd
        // Request audio playout focus (without ducking) and install listener for changes in focus.
        int result = audioManager.requestAudioFocus(audioFocusChangeListener, AppRTCAudioManager.SELECTED_SOUND_CHANNEL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        //setStreamVolume değiştirme işlemi

       // int max = audioManager.getStreamMaxVolume( AudioManager.STREAM_VOICE_CALL );

      //  Log.d(Constants.LOG_TAG, "fey ses max volume="+max);

        /*int volume_level = audioManager.getStreamVolume(AppRTCAudioManager.SELECTED_SOUND_CHANNEL);

        Log.d(Constants.LOG_TAG, "fey ses setStreamVolume="+volume_level);*/
        // audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume_level-2, 0);



        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(Constants.LOG_TAG, "Audio focus request granted for VOICE_CALL streams");
        } else {
            Log.e(Constants.LOG_TAG, "Audio focus request failed");
        }

        // Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
        // required to be in this mode when playout and/or recording starts for
        // best possible VoIP performance.
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        //burada yapmaya çalıştım
        int max = audioManager.getStreamMaxVolume( AudioManager.STREAM_VOICE_CALL );

        Log.d(Constants.LOG_TAG, "fey ses max volume="+max);

        // int volume_level = audioManager.getStreamVolume(AppRTCAudioManager.SELECTED_SOUND_CHANNEL);

        //Log.d(Constants.LOG_TAG, "fey ses volume_level="+volume_level);
       // audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume_level-2, 0);


//        audioManager.setStreamVolume(AudioManager.MODE_IN_COMMUNICATION, 1, 0);
//        int VOLUME = (int) (audioManager.getStreamMaxVolume(Constants.SELECTED_SOUND_CHANNEL) / 2);
//        int VOLUME = (int) (audioManager.getStreamMaxVolume(Constants.SELECTED_SOUND_CHANNEL));
//        audioManager.setStreamVolume(Constants.SELECTED_SOUND_CHANNEL, VOLUME, 0);
        // Always disable microphone mute during a WebRTC call.
        setMicrophoneMute(false);

        // Set initial device states.
        userSelectedAudioDevice = AudioDevice.NONE;
        selectedAudioDevice = AudioDevice.NONE;
        audioDevices.clear();

        // Do initial selection of audio device. This setting can later be changed
        // either by adding/removing a BT or wired headset or by covering/uncovering
        // the proximity sensor.
        updateAudioDeviceState();

        // Register receiver for broadcast intents related to adding/removing a
        // wired headset.
//        registerReceiver(wiredHeadsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        Log.d(Constants.LOG_TAG, "AudioManager started");
    }

    @SuppressWarnings("deprecation")
    // TODO(henrika): audioManager.abandonAudioFocus() is deprecated.
    public void stop() {
        Log.d(Constants.LOG_TAG, "stop");
        ThreadUtils.checkIsOnMainThread();
        if (amState != AudioManagerState.RUNNING) {
            Log.e(Constants.LOG_TAG, "Trying to stop AudioManager in incorrect state: " + amState);
            return;
        }
        amState = AudioManagerState.UNINITIALIZED;

//        unregisterReceiver(wiredHeadsetReceiver);


        // Restore previously stored audio states.
        setSpeakerphoneOn(savedIsSpeakerPhoneOn);
        setMicrophoneMute(savedIsMicrophoneMute);
        audioManager.setMode(savedAudioMode);

        // Abandon audio focus. Gives the previous focus owner, if any, focus.
        audioManager.abandonAudioFocus(audioFocusChangeListener);
        audioFocusChangeListener = null;
        Log.d(Constants.LOG_TAG, "Abandoned audio focus for VOICE_CALL streams");


        audioManagerEvents = null;
        Log.d(Constants.LOG_TAG, "AudioManager stopped");
    }

    /**
     * Changes selection of the currently active audio device.
     */
    private void setAudioDeviceInternal(AudioDevice device) {
        Log.d(Constants.LOG_TAG, "setAudioDeviceInternal(device=" + device + ")");
        assertIsTrue(audioDevices.contains(device));

        switch (device) {
            case SPEAKER_PHONE:
                setSpeakerphoneOn(true);
                break;
            case EARPIECE:
                setSpeakerphoneOn(false);
                break;
            case WIRED_HEADSET:
                setSpeakerphoneOn(false);
                break;
            case BLUETOOTH:
                setSpeakerphoneOn(false);
                break;
            default:
                Log.e(Constants.LOG_TAG, "Invalid audio device selection");
                break;
        }
        selectedAudioDevice = device;
    }

    private void assertIsTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }

    private void logDeviceInfo() {
        Log.d(Constants.LOG_TAG, "Android SDK: " + Build.VERSION.SDK_INT + ", "
                + "Release: " + Build.VERSION.RELEASE + ", "
                + "Brand: " + Build.BRAND + ", "
                + "Device: " + Build.DEVICE + ", "
                + "Id: " + Build.ID + ", "
                + "Hardware: " + Build.HARDWARE + ", "
                + "Manufacturer: " + Build.MANUFACTURER + ", "
                + "Model: " + Build.MODEL + ", "
                + "Product: " + Build.PRODUCT);
    }

    /**
     * Changes default audio device.
     * TODO(henrika): add usage of this method in the AppRTCMobile client.
     */
    public void setDefaultAudioDevice(AudioDevice defaultDevice) {
        ThreadUtils.checkIsOnMainThread();
        switch (defaultDevice) {
            case SPEAKER_PHONE:
                defaultAudioDevice = defaultDevice;
                break;
            case EARPIECE:
                if (hasEarpiece()) {
                    defaultAudioDevice = defaultDevice;
                } else {
                    defaultAudioDevice = AudioDevice.SPEAKER_PHONE;
                }
                break;
            default:
                Log.e(Constants.LOG_TAG, "Invalid default audio device selection");
                break;
        }
        Log.d(Constants.LOG_TAG, "setDefaultAudioDevice(device=" + defaultAudioDevice + ")");
        updateAudioDeviceState();
    }

    /**
     * Changes selection of the currently active audio device.
     */
    public void selectAudioDevice(AudioDevice device) {
        ThreadUtils.checkIsOnMainThread();
        if (!audioDevices.contains(device)) {
            Log.e(Constants.LOG_TAG, "Can not select " + device + " from available " + audioDevices);
        }
        userSelectedAudioDevice = device;
        updateAudioDeviceState();
    }

    /**
     * Returns current set of available/selectable audio devices.
     */
    public Set<AudioDevice> getAudioDevices() {
        ThreadUtils.checkIsOnMainThread();
        return Collections.unmodifiableSet(new HashSet<>(audioDevices));
    }

    /**
     * Returns the currently selected audio device.
     */
    public AudioDevice getSelectedAudioDevice() {
        ThreadUtils.checkIsOnMainThread();
        return selectedAudioDevice;
    }

    /**
     * Helper method for receiver registration.
     */
    private void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        apprtcContext.registerReceiver(receiver, filter);
    }

    /**
     * Helper method for unregistration of an existing receiver.
     */
    private void unregisterReceiver(BroadcastReceiver receiver) {
        apprtcContext.unregisterReceiver(receiver);
    }

    /**
     * Sets the speaker phone mode.
     */
    private void setSpeakerphoneOn(boolean on) {
        boolean wasOn = audioManager.isSpeakerphoneOn();
        if (wasOn == on) {
            return;
        }
        audioManager.setSpeakerphoneOn(on);
    }

    /**
     * Sets the microphone mute state.
     */
    private void setMicrophoneMute(boolean on) {
        boolean wasMuted = audioManager.isMicrophoneMute();
        if (wasMuted == on) {
            return;
        }
        audioManager.setMicrophoneMute(on);
    }

    /**
     * Gets the current earpiece state.
     */
    private boolean hasEarpiece() {
        return apprtcContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    /**
     * Checks whether a wired headset is connected or not.
     * This is not a valid indication that audio playback is actually over
     * the wired headset as audio routing depends on other conditions. We
     * only use it as an early indicator (during initialization) of an attached
     * wired headset.
     */
    @Deprecated
    private boolean hasWiredHeadset() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return audioManager.isWiredHeadsetOn();
        } else {
            final AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL);
            for (AudioDeviceInfo device : devices) {
                final int type = device.getType();
                if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                    Log.d(Constants.LOG_TAG, "hasWiredHeadset: found wired headset");
                    return true;
                } else if (type == AudioDeviceInfo.TYPE_USB_DEVICE) {
                    Log.d(Constants.LOG_TAG, "hasWiredHeadset: found USB audio device");
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Updates list of possible audio devices and make new device selection.
     * TODO(henrika): add unit test to verify all state transitions.
     */
    public void updateAudioDeviceState() {
        ThreadUtils.checkIsOnMainThread();

        Log.d(Constants.LOG_TAG, "Device status: "
                + "available=" + audioDevices + ", "
                + "selected=" + selectedAudioDevice + ", "
                + "user selected=" + userSelectedAudioDevice);

        // Check if any Bluetooth headset is connected. The internal BT state will
        // change accordingly.
        // TODO(henrika): perhaps wrap required state into BT manager.


        // Update the set of available audio devices.
        Set<AudioDevice> newAudioDevices = new HashSet<>();


        if (hasWiredHeadset) {
            // If a wired headset is connected, then it is the only possible option.
            newAudioDevices.add(AudioDevice.WIRED_HEADSET);
        } else {
            // No wired headset, hence the audio-device list can contain speaker
            // phone (on a tablet), or speaker phone and earpiece (on mobile phone).
            newAudioDevices.add(AudioDevice.SPEAKER_PHONE);
            if (hasEarpiece()) {
                newAudioDevices.add(AudioDevice.EARPIECE);
            }
        }
        // Store state which is set to true if the device list has changed.
        boolean audioDeviceSetUpdated = !audioDevices.equals(newAudioDevices);
        // Update the existing audio device set.
        audioDevices = newAudioDevices;
        // Correct user selected audio devices if needed.

        if (hasWiredHeadset && userSelectedAudioDevice == AudioDevice.SPEAKER_PHONE) {
            // If user selected speaker phone, but then plugged wired headset then make
            // wired headset as user selected device.
            userSelectedAudioDevice = AudioDevice.WIRED_HEADSET;
        }
        if (!hasWiredHeadset && userSelectedAudioDevice == AudioDevice.WIRED_HEADSET) {
            // If user selected wired headset, but then unplugged wired headset then make
            // speaker phone as user selected device.
            userSelectedAudioDevice = AudioDevice.SPEAKER_PHONE;
        }

        // Need to start Bluetooth if it is available and user either selected it explicitly or
        // user did not select any output device.
        boolean needBluetoothAudioStart = false;

        // Need to stop Bluetooth audio if user selected different device and
        // Bluetooth SCO connection is established or in the process.
        boolean needBluetoothAudioStop = false;


        // Update selected audio device.
        final AudioDevice newAudioDevice;


        newAudioDevice = defaultAudioDevice;

        // Switch to new device but only if there has been any changes.
        if (newAudioDevice != selectedAudioDevice || audioDeviceSetUpdated) {
            // Do the required device switch.
            setAudioDeviceInternal(newAudioDevice);
            Log.d(Constants.LOG_TAG, "New device status: "
                    + "available=" + audioDevices + ", "
                    + "selected=" + newAudioDevice);
            if (audioManagerEvents != null) {
                // Notify a listening client that audio device has been changed.
                audioManagerEvents.onAudioDeviceChanged(selectedAudioDevice, audioDevices);
            }
        }
        Log.d(Constants.LOG_TAG, "--- updateAudioDeviceState done");
    }
}
