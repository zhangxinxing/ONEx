package ONExClient.onex4j.Daemon;

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
public class TONExDaemon {
    public static void main(String[] args) {
        ONExDaemon daemon = new ONExDaemon(
                new MessageHandler(),
                new TopologyDealer(new SwitchDealer()),
                new SwitchDealer()
        );

        byte[] ba = {0x01,0x01,0x01};
        OFPacketIn pi = new OFPacketIn();
        pi.setReason(OFPacketIn.OFPacketInReason.NO_MATCH);
        pi.setPacketData(ba);
        ONExPacket msg = ONExProtocolFactory.ONExSparePI(pi);

        daemon.sendONEx(msg);

    }

    static void log(Object obj){
        System.out.println(obj);
    }
}
