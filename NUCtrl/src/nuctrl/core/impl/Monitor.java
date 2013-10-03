package nuctrl.core.impl;

import nuctrl.protocol.CoreStatus;

public class Monitor {
	// get Status from
	private CoreStatus status;

    public static int getCpuAccount(){
        return -1;
    }

    public static int getSizeOfQueueIn(){
        return -1;
    }

	public CoreStatus getStatus() {
		return status;
	}
	
	public CoreStatus getNetworkIO() {
		return null;
	}
	
	public CoreStatus getCpuResource() {
		return null;
	}
	
	public String getControllerInfo(){
		return "This is controller";
	}
	
	public boolean isCpuBusy(){
		return false;
	}
	
}
