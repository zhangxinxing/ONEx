package nuctrl.protocol;

public enum MessageType {
	PACKET_IN(0),
	PACKET_OUT(1);

    private static final MessageType[] Types = MessageType.values();
    private static final byte MIN = 0;
    private static final byte MAX = 1;

	private byte value;
	
	private MessageType(int value){
		this.value = (byte)value;
	}
	
	public byte getType(){
		return value;
	}

    public static MessageType fromByte(byte Type){
        if (Type <= MAX && Type >= MIN){
            return Types[Type];
        }
        else{
            System.err.println("Type out of bound");
            return null;
        }
    }
}
