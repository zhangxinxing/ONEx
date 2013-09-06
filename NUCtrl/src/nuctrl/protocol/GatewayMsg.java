package nuctrl.protocol;


import java.nio.*;


public class GatewayMsg {
	String from;
	String to;
	String short_info;
	//OFMessage msg;
	
	public GatewayMsg(String from, String to) {
		super();
		this.from = from;
		this.to = to;
	}
	
	
	public GatewayMsg(String from, String to, ByteBuffer buf){
		// FIXME impl requires
		super();
		this.from = from;
		this.to = to;
	}


	public void writeTo(ByteBuffer buf){
//		TODO msg.writeTo(buf);
	}
}

class OFMessage {
	
}
