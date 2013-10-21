package ONExClient.onex4j.Interface;

import ONExProtocol.ONExPacket;
import org.openflow.protocol.OFMessage;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-18
 * Time: PM11:42
 * To change this template use File | Settings | File Templates.
 */
public interface IONExDaemon {
    void sendONEx(ONExPacket OP);

    void dispatchONEx(ONExPacket op);

    void sparePacketIn(OFMessage msg);

}
