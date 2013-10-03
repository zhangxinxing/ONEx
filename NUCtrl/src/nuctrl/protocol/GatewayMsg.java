package nuctrl.protocol;


import java.nio.ByteBuffer;


public class GatewayMsg {
	
	// basic information for each
	public static final int LEN_header = 9;
	public static final int OFF_LENGTH = 1;
	
	// field of Msg
	/* header */
	private byte type;
	private short from;
	private short to;
	private int	length;
	/* attach */
	private byte[] ofm;
	
	//Raw 
	public GatewayMsg(byte type, short from, short to){
		super();
		this.type = type;
		this.from = from;
		this.to = to;
		this.length = GatewayMsg.LEN_header;
		this.ofm = null;
		
	}
	
	//With OFM
	public GatewayMsg(byte type, short from, short to, byte[] ofm){
		super();
		
		this.type = type;
		this.from = from;
		this.to= to;
		this.length = GatewayMsg.LEN_header;
		
		this.attachOFMessage(ofm);
	}

	// basic write to
	public void writeTo(ByteBuffer buf){
		buf.put(this.type);
		buf.putInt(this.length);
		buf.putShort(this.from);
		buf.putShort(this.to);
		if (this.ofm != null)
			buf.put(this.ofm);
	}
	
	public void attachOFMessage(byte[] ofMsg){
		if (ofMsg != null){
			this.ofm = ofMsg;
			this.length += ofMsg.length;
		}
	}
	
	
	public byte getType(){
		return this.type;
	}
	
	public short getTo(){
		return this.to;
	}
	
	public ByteBuffer toBuffer(){
		ByteBuffer buf = ByteBuffer.allocate(this.length);
		this.writeTo(buf);
		return buf;
	}
	
	public String toString(){
		return String.format(
				"GatewayMsg: %d: %d --> %d",
				this.type,
				this.from, this.to);
	}
	
}

