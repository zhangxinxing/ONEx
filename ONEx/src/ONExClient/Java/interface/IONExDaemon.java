package ONExClient.Java.Interface;

import ONExClient.Java.ONExProtocol;
import org.openflow.protocol.OFMessage;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-18
 * Time: PM11:42
 * To change this template use File | Settings | File Templates.
 */
public interface IONExDaemon {
    void sendONEx(ONExProtocol OP) throws IOException;

    void parseONEx();

    void sparePacketIn(OFMessage msg);

}
