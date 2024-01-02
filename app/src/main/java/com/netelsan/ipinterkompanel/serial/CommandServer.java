package com.netelsan.ipinterkompanel.serial;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Files;
import android.util.Log;
import android.view.Surface;


public class CommandServer implements Runnable {
	
	private static final String TAG = "CommandServer";
    private static final int COMMAND_TCP_PORT = 62520;

    private ServerSocket ss;
    private Socket cs;
    
    private Handler uiMsgHandler;
	
    
    private boolean bShutDown;
       
    public String strClientIp;
    
    private boolean bMsgReady;
	private String strMsg;
	
	OutputStreamWriter osw;
	
    public CommandServer(Handler msgHandler) {
    	 ss = null;
    	 cs = null;
    	 
    	 this.uiMsgHandler = msgHandler;
    	 this.bShutDown = false;
    }
    
    public void shutdown() {
    	
    	Log.i(TAG, "Shutting itself down !");
    	
    	this.bShutDown = true;
    	
    	try {
			if (cs != null && cs.isConnected())
				cs.close();

			ss.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    }
    
    public void SendVideoCallEnd() {
		Log.i(TAG, "SendVideoCallEnd");

		strMsg = "{\"cmd\":\"VideoCallEnd\"}";
		//bMsgReady = true;
		
		try {
			osw.write(strMsg);
			osw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
    
    public void run() {
    	char[] buffer = new char[1520];
    	int ret = 0;
    	String welcomeMsg = "";
    	
    	try {
    		
    		// Initialize Server Socket to listen to its opened port
        	ss = new ServerSocket(COMMAND_TCP_PORT);
        	Log.i(TAG, "CommandServer started on port " + COMMAND_TCP_PORT);
        	
        	while (bShutDown == false) {
	        	// Accept any client connection
	        	cs = ss.accept();
	        	strClientIp = cs.getInetAddress().getHostAddress();
	        	Log.i(TAG, "Client connected from " + strClientIp);
	        	
	        	// Initialize Buffered reader to read the message from the client
	        	BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
	        	osw = new OutputStreamWriter(cs.getOutputStream());
	        	
	        	// Send welcome message to the connected client
	        	if (Application.hostDevice.type == IpDeviceNode.DeviceType.DOOR_PANEL)
	        		welcomeMsg = "{\"devType\":\"PANEL\"}";
	        	else if (Application.hostDevice.type == IpDeviceNode.DeviceType.MONITOR)
	        		welcomeMsg = "{\"devType\":\"MONITOR\"}";
	        	
	        	osw.write(welcomeMsg);
	        	osw.flush();
	        	
	        	// Get incoming message
	        	while ((ret = in.read(buffer)) != -1) {
	        		String msg = String.valueOf(buffer, 0, ret);
	        		
	        		// Use incomingMessage as required
		        	Log.i(TAG, msg);
		        	
		        	ParseCommand(msg);
	        	}
	        	
	        	 
	        	Log.i(TAG, "Client Disconnected !");
	        	 
	        	// Close input stream
	        	in.close();
	        	
	        	// Close Socket
	        	cs.close();
        	}
        	
        	
        	// Close server socket
			ss.close();
		} catch (IOException e) {
			
			Log.w(TAG, "Shutting down with exception !");
			
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
    	
    	
    }

    
    private void ParseCommand(String jsonStr) {
		
    	try {
    		
    		JSONObject jObject = new JSONObject(jsonStr);
    		
    		// parse DEVICE TYPE
    		if (jObject.has("devType")) {
    			String devStr = jObject.getString("devType");
    			if (devStr.equals("PANEL")) {
    				Application.remoteDevice.type = IpDeviceNode.DeviceType.DOOR_PANEL;
    				Log.i(TAG, "remoteDevice: DOOR_PANEL");
    			}
    			else if (devStr.equals("MONITOR")) {
    				Application.remoteDevice.type = IpDeviceNode.DeviceType.MONITOR;
    				Log.i(TAG, "remoteDevice: MONITOR");
    			}
    		}
    		
    		// parse COMMAND
    		if (jObject.has("cmd")) {
				String cmdStr = jObject.getString("cmd");
				if (cmdStr.equals("VideoCallStart")) {
					Log.i(TAG, "Video Call START --> " + this.strClientIp);
					
					Message msg = uiMsgHandler.obtainMessage();
					msg.what = Application.UIMSG_VIDEOCALL_START;
					
					Bundle bundle = new Bundle();
					bundle.putString("remoteIp", this.strClientIp);
					msg.setData(bundle);
					
					uiMsgHandler.sendMessage(msg);
				}	
				else if (cmdStr.equals("VideoCallEnd")) {
					Log.i(TAG, "Video Call END --> " );
	
					Message msg = uiMsgHandler.obtainMessage();
					msg.what = Application.UIMSG_VIDEOCALL_END;
					
					Bundle bundle = new Bundle();
					bundle.putString("remoteIp", this.strClientIp);
					msg.setData(bundle);		
					
					uiMsgHandler.sendMessage(msg);
				}
				else if (cmdStr.equals("DoorUnlock")) {
					Log.i(TAG, "DOOR UNLOCK received" );
					
//					Application.keypad.sendDoorUnlock();
				}	
				else
					Log.w(TAG, "Cannot parse command --> " + jsonStr);
    		}
    		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.w(TAG, "Unformatted string from client --> " + jsonStr);
		}
    	

//    	To get a specific string
//
//    	String aJsonString = jObject.getString("STRINGNAME");
//
//    	To get a specific boolean
//
//    	boolean aJsonBoolean = jObject.getBoolean("BOOLEANNAME");
//
//    	To get a specific integer
//
//    	int aJsonInteger = jObject.getInt("INTEGERNAME");
//
//    	To get a specific long
//
//    	long aJsonLong = jObject.getBoolean("LONGNAME");
//
//    	To get a specific double
//
//    	double aJsonDouble = jObject.getDouble("DOUBLENAME");
//
//    	To get a specific JSONArray:
//
//    	JSONArray jArray = jObject.getJSONArray("ARRAYNAME");
//
//    	To get the items from the array
//
//    	for (int i=0; i < jArray.length(); i++)
//    	{
//    	    try {
//    	        JSONObject oneObject = jArray.getJSONObject(i);
//    	        // Pulling items from the array
//    	        String oneObjectsItem = oneObject.getString("STRINGNAMEinTHEarray");
//    	        String oneObjectsItem2 = oneObject.getString("anotherSTRINGNAMEINtheARRAY");
//    	    } catch (JSONException e) {
//    	        // Oops
//    	    }
//    	}
	}
    
}
