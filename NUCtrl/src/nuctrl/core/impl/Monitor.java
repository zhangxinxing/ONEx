package nuctrl.core.impl;

public class Monitor {
	// get Status from
    public static int getCpuAccount(){
        return -1;
    }

    public static int getSizeOfQueueIn(){
        return -1;
    }


	public String getControllerInfo(){
		return "This is controller";
	}

	public boolean isCpuBusy(){
		return false;
	}
	
}
