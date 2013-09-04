package nuctrl.protocol;


import java.nio.*;


public class GatewayMsg {
	char from;
	char to;
	OFMessage msg;
	
	void writeTo(ByteBuffer buf){
		buf.putChar(from);
		buf.putChar(to);
//		TODO msg.writeTo(buf);
	}
}

class OFMessage {
	
}
