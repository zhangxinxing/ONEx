package nuctrl.core.test;

import nuctrl.core.debug.Dump;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.GatewayMsgFactory;
import nuctrl.protocol.GatewayMsgType;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

public class TGatewayMsg {

	public static void main(String[] args) {
		// Test writing and reading Msg from/to buffer
		
		ByteBuffer buf = ByteBuffer.allocate(1024);
		
		GatewayMsg msg1 = GatewayMsgFactory.getGatewatMsg(GatewayMsgType.HELLO, (short)1, (short)2);
		GatewayMsg msg2 = GatewayMsgFactory.getGatewatMsg(GatewayMsgType.HELLO, (short)8, (short)9);

		
		System.out.println(Dump.buf(buf));
		msg1.writeTo(buf);  System.out.println(Dump.buf(buf));
		msg2.writeTo(buf);  System.out.println(Dump.buf(buf));
		buf.flip();			System.out.println(Dump.buf(buf));
		
		List list = GatewayMsgFactory.parseGatewayMsg(buf);
		
		if (list != null){
			Iterator iter = list.iterator();
			while (iter.hasNext()){
				GatewayMsg msg = (GatewayMsg) iter.next();
				System.out.println(msg.toString());
				iter.remove();
			}
		}
	}

}
