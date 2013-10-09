package nuctrl.test;

import nuctrl.datastruct.Buffer;
import nuctrl.debug.Dump;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.GatewayMsgFactory;
import nuctrl.protocol.MessageType;

import java.nio.ByteBuffer;

public class TSafeCopy {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ByteBuffer buf = ByteBuffer.allocate(33);
		
		GatewayMsg msg = GatewayMsgFactory.getGatewatMsg(MessageType.HELLO, (short)1, (short)1);
		
		msg.writeTo(buf);
		
		msg.writeTo(buf);
		msg.writeTo(buf);
		buf.put((byte)0);
		buf.putInt(100);
		//p = 27 now
		buf.flip();
		
		System.out.println(Dump.buf(buf));
		/* Buf <p>=0 <limit>=32 <cap>=33 <len>=32 */
		
		ByteBuffer buf_cp = Buffer.safeClone(buf);
		
		System.out.println(Dump.buf(buf_cp));
		/* Buf <p>=0 <limit>=27 <cap>=27 <len>=27 */
		
		System.out.println(Dump.buf(buf));
		/* Buf <p>=27 <limit>=32 <cap>=33 <len>=5 */
				

	}

}
