package ONExProtocol;

import java.nio.ByteBuffer;

/**
 * User: fan
 * Date: 13-10-24
 * Time: AM9:19
 */
public interface ITLV {

    public int getLength();

    public ByteBuffer toByteBuffer();
}
