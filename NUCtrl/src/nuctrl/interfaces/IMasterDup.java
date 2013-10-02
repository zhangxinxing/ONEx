package nuctrl.interfaces;

import nuctrl.protocol.CoreStatus;


// inferface to communicate with IO_Master
public interface IMasterDup {
	
	String getControllerInfo();
	
	CoreStatus getStatus();
	
	void onMsgFromMaster();
	
	void sendToMaster();
	
}
