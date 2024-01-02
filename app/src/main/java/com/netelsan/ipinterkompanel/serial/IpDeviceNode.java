package com.netelsan.ipinterkompanel.serial;

public class IpDeviceNode {
	
	public enum DeviceType{
		  MONITOR, 
		  DOOR_PANEL, 
		  SECURITY_CONSOLE
	}
	
	public DeviceType type = DeviceType.MONITOR;
	public String address;
	public String name;
}
