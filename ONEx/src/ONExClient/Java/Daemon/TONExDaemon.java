package ONExClient.Java.Daemon;

import MyDebugger.Dumper;
import ONExClient.Java.MessageHandler;
import ONExClient.Java.SwitchDealer;
import ONExClient.Java.TopologyDealer;
import ONExProtocol.ONExPacket;
import ONExProtocol.ONExProtocolFactory;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.action.OFAction;

import java.util.LinkedList;

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

        OFFlowMod ofFlowMod = new OFFlowMod();
        ofFlowMod.setMatch(new OFMatch());
        OFPacketOut po = new OFPacketOut();
        po.setActions(new LinkedList<OFAction>());

        ONExPacket msg = ONExProtocolFactory.ONExResSparePI(ofFlowMod, po);
        log(msg.toString());
        log(Dumper.byteArray(msg.toByteArray()));

        daemon.sendONEx(msg);

    }

    static void log(Object obj){
        System.out.println(obj);
    }
}
