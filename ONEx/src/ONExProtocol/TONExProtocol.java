package ONExProtocol;

import ONExClient.onex4j.GlobalTopo;
import ONExClient.onex4j.LocalTopo;
import org.openflow.protocol.*;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.factory.BasicFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: PM12:36
 */
public class TONExProtocol {

    private static Logger log = Logger.getLogger(TONExProtocol.class);

    public static void main(String[] args){
        BasicFactory factory = new BasicFactory();

        // 1
        byte[] ba = {0x01,0x01,0x01};
        OFPacketIn pi = new OFPacketIn();
        pi.setReason(OFPacketIn.OFPacketInReason.NO_MATCH);
        pi.setPacketData(ba);
        ONExPacket msg = ONExProtocolFactory.ONExSparePI(pi);
        msg.setSrcHost(new InetSocketAddress("127.1.2.3", 1234));
        log.info(msg.toString());

        ByteBuffer msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        msg = ONExProtocolFactory.parser(msgBB);
        log.info(msg.toString());
        log.info("extractd ipv4:" + msg.getSrcHost().toString());

        // 2
        OFFlowMod ofFlowMod = new OFFlowMod();
        ofFlowMod.setMatch(new OFMatch());
        OFPacketOut po = new OFPacketOut();
        po.setActions(new LinkedList<OFAction>());

        msg = ONExProtocolFactory.ONExResSparePI(ofFlowMod, po);
        msg.setSrcHost(new InetSocketAddress("127.1.2.3", 1234));
        log.info(msg.toString());
        msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log.info(ONExProtocolFactory.parser(msgBB).toString());
        log.info(msg.getFlowMod());//get flowmod
        log.info(msg.getOFPacketOut());

        // 3
        LocalTopo topo = new LocalTopo();
        msg = ONExProtocolFactory.ONExUploadLocalTopo(topo);
        log.info(msg.toString());
        msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log.info(ONExProtocolFactory.parser(msgBB).toString());

        // 4
        msg = ONExProtocolFactory.ONExGetGlobalTopo();
        log.info(msg.toString());
        msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log.info(ONExProtocolFactory.parser(msgBB).toString());

        // 5
        GlobalTopo globalTopo = new GlobalTopo();
        msg = ONExProtocolFactory.ONExResGlobalTopo(globalTopo);
        log.info(msg.toString());
        msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log.info(ONExProtocolFactory.parser(msgBB).toString());

        // 6
        msg = ONExProtocolFactory.ONExReqGlobalFlowMod();
        log.info(msg.toString());
        msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log.info(ONExProtocolFactory.parser(msgBB).toString());

        // 7
        msg = ONExProtocolFactory.ONExSCFlowMod(ofFlowMod);
        log.info(msg.toString());
        msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log.info(ONExProtocolFactory.parser(msgBB).toString());
    }
}
