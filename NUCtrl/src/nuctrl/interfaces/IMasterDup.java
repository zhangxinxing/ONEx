package nuctrl.interfaces;


// inferface to communicate with IO_Master
public interface IMasterDup {
	
	String getControllerInfo();
	

	void onMsgFromMaster();
	
	void sendToMaster();
	
}
