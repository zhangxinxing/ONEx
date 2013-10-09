package nuctrl.protocol;

public enum MessageType {
	PACKET_IN(0),
	PACKET_OUT(1);
	
	private byte value;
	
	private MessageType(int value){
		this.value = (byte)value;
	}
	
	public byte getType(){
		return value;
	}
	
}
