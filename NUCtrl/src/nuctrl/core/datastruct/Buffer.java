package nuctrl.core.datastruct;

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
}
