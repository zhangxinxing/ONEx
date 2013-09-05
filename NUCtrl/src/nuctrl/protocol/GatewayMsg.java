package nuctrl.protocol;


import java.nio.*;


public class GatewayMsg {
	char from;
	char to;
	//OFMessage msg;
	
	public GatewayMsg(char from, char to) {
		super();
		this.from = from;
		this.to = to;
	}
	
	
	public void writeTo(ByteBuffer buf){
		buf.putChar(from);
		buf.putChar(to);
//		TODO msg.writeTo(buf);
	}
}

class OFMessage {
	
}
