package com.netelsan.ipinterkompanel.serial;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;


public class CommandClient implements Runnable {

	private static final String TAG = "CommandClient";
	private static final boolean DEBUG_SEND_FRAME = false;
	private static final boolean DEBUG_SEND_PACKET = false;

	public static final int COMMAND_TCP_PORT = 62520;

	private Socket cs;

	private boolean bShutDown;

	private enum MessgeRcvState {
		MSG_STAT_WAIT_HDR, MSG_STAT_WAIT_DATA,
	}

	private MessgeRcvState msgStat;

	private OutputStreamWriter out;
	BufferedReader in = null;
	private boolean bMsgReady;
	private String strMsg = null;

	private String strClientIpAddr;
	private boolean bConnect;
	
	
	public CommandClient() {
		this.bShutDown = false;
		this.bMsgReady = false;
		this.bConnect = false;
		cs = null;
	}

	public void shutdown() {

		Log.i(TAG, "Shutting itself down !");

		this.bShutDown = true;

		try {
			if (cs != null && cs.isConnected())
				cs.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void Connect(String ipAddr) {
		strClientIpAddr = ipAddr;	
		
		this.bConnect = true;
	}

//	public void SendVideoCallStart(String ipAddr) {
//		Log.i(TAG, "SendVideoCallStart | " + ipAddr);
//
//		strClientIpAddr = ipAddr;
//		if (Application.hostDevice.type == DeviceType.DOOR_PANEL)
//			strMsg = "{\"cmd\":\"VideoCallStart\",\"devType\":\"PANEL\"}";
//		else if (Application.hostDevice.type == DeviceType.MONITOR)
//			strMsg = "{\"cmd\":\"VideoCallStart\",\"devType\":\"MONITOR\"}";
//		
//		bMsgReady = true;
//		bConnect = true;
//	}
	
	public void SendVideoCallEnd(String ipAddr) {
		Log.i(TAG, "SendVideoCallEnd | " + strClientIpAddr);

		strClientIpAddr = ipAddr;
		strMsg = "{\"cmd\":\"VideoCallEnd\"}";
		bConnect = true;
	}
	
	public void SendDoorUnlock(String ipAddr) {
		Log.i(TAG, "SendVideoCallEnd | " + strClientIpAddr);

		strClientIpAddr = ipAddr;
		strMsg = "{\"cmd\":\"DoorUnlock\"}";
		bConnect = true;
	}
	

	public void run() {
		
		char[] buffer = new char[256];
		int readSize;
		
		while (bShutDown == false) {
			
			try {
				if (bConnect) {
					
					if (cs != null) {
						out.close();
						cs.close();
						cs = null;
					}
					
					// Initialize client Socket to connect to remote server
					InetAddress serverAddr = InetAddress.getByName(strClientIpAddr);
			
					Log.d(TAG, "Connecting... " + strClientIpAddr);
			
					// create a socket to make the connection with the server
					cs = new Socket(serverAddr, COMMAND_TCP_PORT);					
			
					Log.i(TAG, "Conmmand server connection successfull");
			
					// Initialize Buffered reader to read the message from the client
					in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
					out = new OutputStreamWriter(cs.getOutputStream());
					
					// Get welcome message and parse device type respectively					
					readSize = in.read(buffer);
	        		String welcomeMsg = String.valueOf(buffer, 0, readSize);
	        		
	        		JSONObject jObject = new JSONObject(welcomeMsg);
	            	
	    			String cmdStr = jObject.getString("devType");
	    			if (cmdStr.equals("PANEL")) {
	    				Application.remoteDevice.type = IpDeviceNode.DeviceType.DOOR_PANEL;
	    				Log.i(TAG, "remote device type: PANEL");
	    			}
	    			else if (cmdStr.equals("MONITOR")) {
	    				Application.remoteDevice.type = IpDeviceNode.DeviceType.MONITOR;
	    				Log.i(TAG, "remote device type: MONITOR");
	    			}
	    			
	    			// If any message is waiting send it
					if (strMsg != null) {
						out.write(strMsg);
						out.flush();
					}
					
					// Disconnect					
					out.close();
					in.close();
					cs.close();
					bConnect = false;	
				}
				
			} catch (IOException e) {

				Log.w(TAG, "Shutting down with exception !");

				//Log.i(TAG, "Client Disconnected !");

				
				// Retry connection after 500ms
				try {
					// Close input stream
					if (out != null)
						out.close();

					// Close Socket
					if (cs != null)
						cs.close();
					
					Thread.sleep(500);
				} catch (InterruptedException e1) {
				} catch (IOException e1) {
				}

				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			// Relax CPU when no task is executed !!!
			// TODO: replace with asynctask or one shut thread			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}

		Log.w(TAG, "Shutting down main loop !");

	}

}
