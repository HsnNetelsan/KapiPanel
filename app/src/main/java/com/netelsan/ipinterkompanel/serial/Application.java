package com.netelsan.ipinterkompanel.serial;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import android.os.Handler;
import android.util.Log;

public class Application {
	
	public static CommandServer commandServer;
	public static CommandClient commandClient;
//	public static KeypadAccess keypad;
	
	public static Handler uiHandler;
	
	
	// Set default stream state to IDLE
	public static IpDeviceNode hostDevice = new IpDeviceNode();
	public static IpDeviceNode remoteDevice = new IpDeviceNode();
	
	public static final int UIMSG_VIDEOCALL_START = 0;
	public static final int UIMSG_VIDEOCALL_END = 1;
	public static final int UIMSG_KEY_PRESSED = 2;
	public static final int UIMSG_RFID_TAG = 3;
	
	public static boolean getListOfNetworkInterfaces() {

//        List<String> list = new ArrayList<String>();
//
//        Enumeration<NetworkInterface> nets;
//
//        try {
//            nets = NetworkInterface.getNetworkInterfaces();
//        } catch (SocketException e) {
//
//            e.printStackTrace();
//            return null;
//        }
//
//        for (NetworkInterface netint : Collections.list(nets)) {
//            list.add(netint.getName());
//            Log.i("getListOfNetworkInterfaces", "Found: " + netint.getName());
//        }
//
//        return list;
		
		try {

            String line;
            boolean r = false;

            //Process p = Runtime.getRuntime().exec("netcfg");
            //Process p = Runtime.getRuntime().exec("ifconfig eth0 192.168.6.10");
            Process p = Runtime.getRuntime().exec("/system/xbin/su\n");
                       
//          Process su = Runtime.getRuntime().exec("/system/xbin/su\n");
//			String cmd = "ifconfig eth0 192.168.6.10\n";
//			su.getOutputStream().write(cmd.getBytes());
            			
            BufferedReader input = new BufferedReader (new InputStreamReader(p.getErrorStream()));   
            while ((line = input.readLine()) != null) {   
            	Log.d("OLE-ERR", line);
            	
                if(line.contains("eth0")){
                    if(line.contains("UP")){
                        r=true;
                    }
                    else{
                        r=false;
                    }
                }
            }   
            input.close();

            Log.e("OLE","isEthOn: "+r);
            return r; 

        } catch (IOException e) {
            Log.e("OLE","Runtime Error: "+e.getMessage());
            e.printStackTrace();
            return false;
        }

    }
	
	
	
	public static boolean canRunRootCommands()
	   {
	      boolean retval = false;
	      Process suProcess;

	      try
	      {
	         suProcess = Runtime.getRuntime().exec("su");

	         DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
	         DataInputStream osRes = new DataInputStream(suProcess.getInputStream());

	         if (null != os && null != osRes)
	         {
	            // Getting the id of the current user to check if this is root
	            os.writeBytes("id\n");
	            os.flush();

	            String currUid = osRes.readLine();
	            boolean exitSu = false;
	            if (null == currUid)
	            {
	               retval = false;
	               exitSu = false;
	               Log.d("ROOT", "Can't get root access or denied by user");
	            }
	            else if (true == currUid.contains("uid=0"))
	            {
	               retval = true;
	               exitSu = true;
	               Log.d("ROOT", "Root access granted");
	            }
	            else
	            {
	               retval = false;
	               exitSu = true;
	               Log.d("ROOT", "Root access rejected: " + currUid);
	            }

	            if (exitSu)
	            {
	               os.writeBytes("exit\n");
	               os.flush();
	            }
	         }
	      }
	      catch (Exception e)
	      {
	         // Can't get root !
	         // Probably broken pipe exception on trying to write to output stream (os) after su failed, meaning that the device is not rooted

	         retval = false;
	         Log.d("ROOT", "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
	      }

	      return retval;
	   }
	
	
	
}
