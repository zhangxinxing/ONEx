package ONExProtocol;

import ONExClient.onex4j.GlobalTopo;
import ONExClient.onex4j.LocalTopo;
import org.openflow.protocol.*;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.factory.BasicFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: PM12:36
 */
public class TONExProtocol {
    public static void main(String[] args){
        BasicFactory factory = new BasicFactory();
        // 1
        byte[] ba = {0x01,0x01,0x01};
        OFPacketIn pi = new OFPacketIn();
        pi.setReason(OFPacketIn.OFPacketInReason.NO_MATCH);
        pi.setPacketData(ba);
        ONExPacket msg = ONExProtocolFactory.ONExSparePI(pi);
        msg.setSrcHost(new InetSocketAddress("127.1.2.3", 1234));
        log(msg.toString());

        ByteBuffer msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        msg = ONExProtocolFactory.parser(msgBB);
        log(msg.toString());
        log(msg.getSrcHost().toString());

        // 2
        OFFlowMod ofFlowMod = new OFFlowMod();
        ofFlowMod.setMatch(new OFMatch());
        OFPacketOut po = new OFPacketOut();
        po.setActions(new LinkedList<OFAction>());

        msg = ONExProtocolFactory.ONExResSparePI(ofFlowMod, po);
        log(msg.toString());
        msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log(ONExProtocolFactory.parser(msgBB).toString());

        // 3
        LocalTopo topo = new LocalTopo();
        msg = ONExProtocolFactory.ONExUploadLocalTopo(topo);
        log(msg.toString());
        msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log(ONExProtocolFactory.parser(msgBB).toString());

        // 4
        msg = ONExProtocolFactory.ONExGetGlobalTopo();
        log(msg.toString());
        msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log(ONExProtocolFactory.parser(msgBB).toString());

        // 5
        GlobalTopo globalTopo = new GlobalTopo();
        msg = ONExProtocolFactory.ONExResGlobalTopo(globalTopo);
        log(msg.toString());
        msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log(ONExProtocolFactory.parser(msgBB).toString());

        // 6
        msg = ONExProtocolFactory.ONExReqGlobalFlowMod();
        log(msg.toString());
        msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log(ONExProtocolFactory.parser(msgBB).toString());

        // 7
        msg = ONExProtocolFactory.ONExSCFlowMod(ofFlowMod);
        log(msg.toString());
        msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log(ONExProtocolFactory.parser(msgBB).toString());
    }

    public static void log(Object obj){
        System.out.println(obj);
    }
}
