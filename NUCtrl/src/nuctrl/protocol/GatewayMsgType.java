package nuctrl.protocol;

public enum GatewayMsgType {
	HELLO(0),
	HELLO_ACK(1);
	
	private byte value;
	
	private GatewayMsgType(int value){
		if (value > Byte.MAX_VALUE)
			System.out.println("Error in Enum GatewayMsgType");
		this.value = (byte)value;
	}
	
	public byte getType(){
		return value;
	}
	
}
