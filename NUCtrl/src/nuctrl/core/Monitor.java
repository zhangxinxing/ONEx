package nuctrl.core;

interface IMonitor{
    public boolean isCpuBusy();

}

public class Monitor implements IMonitor{
	// get Status from
    public static int getCpuAccount(){
        return -1;
    }

    public static int getSizeOfQueueIn(){
        return -1;
    }

    @Override
	public boolean isCpuBusy(){
		return false;
	}
	
}
