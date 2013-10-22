package ONExClient.onex4j.Interface;

import ONExProtocol.ONExPacket;
import org.openflow.protocol.OFMessage;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-18
 * Time: PM11:35
 */
public interface IPacketHandler {
    void onLocalPacketIn(OFMessage msg);

    void onRemotePacketIn(ONExPacket msg);

    void onRemotePacketOut(OFMessage msg);

}
