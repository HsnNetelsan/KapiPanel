/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.netelsan.ipinterkompanel.webrtc.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.netelsan.ipinterkompanel.Constants;
import com.netelsan.ipinterkompanel.R;
import com.netelsan.ipinterkompanel.webrtc.AppRTCAudioManager;

import org.webrtc.RendererCommon.ScalingType;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Fragment for call control.
 */
public class CallFragment extends Fragment {
    private TextView contactView;
    private ImageButton cameraSwitchButton;
    private ImageButton videoScalingButton;
    private ImageButton toggleMuteButton;
//    private SeekBar captureFormatSlider;
    private OnCallEvents callEvents;
    private ScalingType scalingType;
    private boolean videoCallEnabled = true;

    String callerIP = "";

    /**
     * Call control interface for container activity.
     */
    public interface OnCallEvents {
        void onCallHangUp();

        void onCameraSwitch();

        void onVideoScalingSwitch(ScalingType scalingType);

        void onCaptureFormatChange(int width, int height, int framerate);

        boolean onToggleMic();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View controlView = inflater.inflate(R.layout.fragment_call, container, false);

        Bundle bundle = getArguments();
        callerIP = bundle.getString("caller_ip");

        // Create UI controls.
        contactView = controlView.findViewById(R.id.contact_name_call);
        ImageButton disconnectButton = controlView.findViewById(R.id.button_call_disconnect);
        cameraSwitchButton = controlView.findViewById(R.id.button_call_switch_camera);
        videoScalingButton = controlView.findViewById(R.id.button_call_scaling_mode);
        toggleMuteButton = controlView.findViewById(R.id.button_call_toggle_mic);
//        captureFormatSlider = controlView.findViewById(R.id.capture_format_slider_call);

        // Add buttons click events.
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callEvents.onCallHangUp();
            }
        });

        cameraSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callEvents.onCameraSwitch();
            }
        });

        videoScalingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(scalingType == ScalingType.SCALE_ASPECT_FILL) {
                    videoScalingButton.setBackgroundResource(R.drawable.ic_action_full_screen);
                    scalingType = ScalingType.SCALE_ASPECT_FIT;
                } else {
                    videoScalingButton.setBackgroundResource(R.drawable.ic_action_return_from_full_screen);
                    scalingType = ScalingType.SCALE_ASPECT_FILL;
                }
                callEvents.onVideoScalingSwitch(scalingType);
            }
        });
        scalingType = ScalingType.SCALE_ASPECT_FILL;

        toggleMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean enabled = callEvents.onToggleMic();
                toggleMuteButton.setAlpha(enabled ? 1.0f : 0.3f);
            }
        });



        return controlView;
    }

    @Override
    public void onStart() {
        super.onStart();

        boolean captureSliderEnabled = false;
        Bundle args = getArguments();
        if(args != null) {

            String contactName = callerIP;

            contactView.setText(contactName);
            videoCallEnabled = true;
            captureSliderEnabled = videoCallEnabled && false;
        }
        if(! videoCallEnabled) {
            cameraSwitchButton.setVisibility(View.INVISIBLE);
        }
//        if(captureSliderEnabled) {
//            captureFormatSlider.setOnSeekBarChangeListener(new CaptureQualityController(callEvents));
//        } else {
//            captureFormatSlider.setVisibility(View.GONE);
//        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callEvents = (OnCallEvents) activity;
    }

}
