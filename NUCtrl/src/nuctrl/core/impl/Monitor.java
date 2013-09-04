package nuctrl.core.impl;

import nuctrl.protocol.CoreStatus;

public class Monitor {
	// get Status from
	private CoreStatus status;
	
	CoreStatus getStatus() {
		return status;
	}
	
	CoreStatus getNetworkIO() {
		return null;
	}
	
	CoreStatus getCpuResource() {
		return null;
	}
	
	String getControllerInfo(){
		return "This is controller";
	}
	
	boolean isCpuBusy(){
		return false;
	}
	
}
