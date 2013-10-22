package ONExClient.onex4j.SDKDaemon;

import MyDebugger.Dumper;
import ONExClient.onex4j.MessageHandler;
import ONExClient.onex4j.SwitchDealer;
import ONExClient.onex4j.TopologyDealer;
import ONExProtocol.ONExPacket;
import ONExProtocol.ONExProtocolFactory;
import org.openflow.protocol.OFPacketIn;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM12:41
 */
public class TSDKDaemon {
    public static void main(String[] args) {
        SDKDaemon daemon = new SDKDaemon(
                12345,
                new MessageHandler(),
                new TopologyDealer(new SwitchDealer()),
                new SwitchDealer()
        );

        byte[] ba = {0x01,0x01,0x01};
        OFPacketIn pi = new OFPacketIn();
        pi.setReason(OFPacketIn.OFPacketInReason.NO_MATCH);
        pi.setPacketData(ba);
        ONExPacket msg = ONExProtocolFactory.ONExSparePI(pi);

        System.err.println(Dumper.byteArray(msg.toByteArray()));
        daemon.sendONEx(msg);

    }

    static void log(Object obj){
        System.out.println(obj);
    }
}
