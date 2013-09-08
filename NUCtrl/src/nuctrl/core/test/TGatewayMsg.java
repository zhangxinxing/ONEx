package nuctrl.core.test;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.apple.crypto.provider.Debug;

import nuctrl.core.debug.Dump;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.GatewayMsgFactory;
import nuctrl.protocol.GatewayMsgType;

public class TGatewayMsg {

	public static void main(String[] args) {
		// Test writing and reading Msg from/to buffer
		
		ByteBuffer buf = ByteBuffer.allocate(1024);
		
		GatewayMsg msg1 = GatewayMsgFactory.getGatewatMsg(GatewayMsgType.HELLO, (short)1, (short)2);
		GatewayMsg msg2 = GatewayMsgFactory.getGatewatMsg(GatewayMsgType.HELLO, (short)8, (short)9);

		
		System.out.println(Dump.dumpBuf(buf));
		msg1.writeTo(buf);  System.out.println(Dump.dumpBuf(buf));
		msg2.writeTo(buf);  System.out.println(Dump.dumpBuf(buf));
		buf.flip();			System.out.println(Dump.dumpBuf(buf));
		
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
