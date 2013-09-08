package nuctrl.protocol;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class GatewayMsgFactory {

	static public GatewayMsg getGatewatMsg(GatewayMsgType type, short from, short to){
		GatewayMsg msg = null;
		switch(type){
		case HELLO:
			msg = new GatewayMsg(type.getType(), from, to);
			return msg;
			
		case HELLO_ACK:
			msg = new GatewayMsg(type.getType(), from, to);
			break;
		
		default:
		}
		
		return msg;
		
	}
	
	static public List<GatewayMsg> parseGatewayMsg(ByteBuffer buf){
		
		List<GatewayMsg> list = new LinkedList<GatewayMsg>();
		
		while(buf.hasRemaining()){
			
			byte type = buf.get();
			int length = buf.getInt();
			byte[] ofm = new byte[length - GatewayMsg.l_header];
			
			short from = buf.getShort();
			short to = buf.getShort();
			buf.get(ofm);
			
			GatewayMsg msg = new GatewayMsg(type,from, to);
			msg.attachOFMessage(ofm);
			
			list.add(msg);
		} //FIXME buf.compact() or not?
				
		return list;
	}
	
	
	private boolean isValidate(ByteBuffer buf){
		int len_should_be = buf.limit() - buf.position();
		int length = buf.getInt(1);
		
		return (len_should_be == length);
	}
}
