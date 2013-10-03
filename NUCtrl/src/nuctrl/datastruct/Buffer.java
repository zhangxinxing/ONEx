package nuctrl.datastruct;

import nuctrl.protocol.GatewayMsg;

import java.nio.ByteBuffer;

public class Buffer {
	public static ByteBuffer clone(ByteBuffer original_buf){
		ByteBuffer buf = ByteBuffer.allocate(original_buf.capacity());
		buf.rewind();
		buf.put(original_buf.array());
		buf.position(original_buf.position());
		buf.limit(original_buf.limit());
		return buf;
	}
	
	public static boolean cloneTo(ByteBuffer from, ByteBuffer to){
		if (to.capacity() < from.capacity()){
			//remains unchanged
			return false;
		}
		else{
			to.rewind();
			to.put(from.array());
			to.position(from.position());
			to.limit(from.limit());
			return true;
		}
	}
	
	/*
	 * original buffer will be extracted out as many as possible GatewayMsg, uncompleted Msg will be left
	 * But original_buf.compact() is left to call thread(!!)
	 * return Bytebuffer, integrated guaranteed
	 */
	public static ByteBuffer safeClone(ByteBuffer original_buf){
		int offset = original_buf.position();
		int max = original_buf.limit();
		while(offset + GatewayMsg.OFF_LENGTH < max){
			int len = original_buf.getInt(offset + GatewayMsg.OFF_LENGTH);
			if (offset + len > max + 1)
				break;
			offset += len;
		}
		byte[] temp = new byte[offset];
		original_buf.get(temp);
		original_buf.position(offset);
		ByteBuffer out = ByteBuffer.wrap(temp);
		return out;
	}
}
