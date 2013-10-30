package ONExProtocol;

import org.apache.log4j.Logger;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.action.OFAction;

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

    private static Logger log = Logger.getLogger(TONExProtocol.class);

    public static void main(String[] args){

        TONExProtocol.ONExSparePI();
        TONExProtocol.ONExResSparePI();
        ln();

        TONExProtocol.ONExUploadLocalTopo();
        ln();

        TONExProtocol.ONExRequestGlobalTopo();
        ln();

        TONExProtocol.ONExReqGlobalFlowMod();
        TONExProtocol.ONExSCFlowMod();
    }

    public static void ONExSparePI(){
        byte[] ba = {0x01,0x01,0x01};
        OFPacketIn pi = new OFPacketIn();
        pi.setReason(OFPacketIn.OFPacketInReason.NO_MATCH);
        pi.setPacketData(ba);
        log.info("before:\t" + pi.toString());
        ONExPacket msg = ONExProtocolFactory.ONExSparePI(
                pi,
                new InetSocketAddress("127.1.2.3", 1234),
                1234567890L
        );
        log.info("build:\t" + msg.toString());
        log.info("dpid: " + msg.getSrcDpid());

        ByteBuffer msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        msg = ONExProtocolFactory.parser(msgBB);
        log.info("rebuild:\t" + msg.toString());
        log.info("getTLV:\t" + msg.getOFPacketIn().toString());
        log.info("extracted ipv4:" + msg.getSrcHost().toString());
    }

    public static void ONExResSparePI(){

        OFFlowMod ofFlowMod = new OFFlowMod();
        ofFlowMod.setMatch(new OFMatch());
        OFPacketOut po = new OFPacketOut();
        po.setBufferId(1);
        po.setActions(new LinkedList<OFAction>());
        log.info("before\t" + po.toString());
        log.info("before\t" + ofFlowMod.toString());

        ONExPacket msg = ONExProtocolFactory.ONExResSparePI(
                ofFlowMod,
                po,
                new InetSocketAddress("127.1.2.3", 1234),
                1234567890L
        );
        log.info("build\t\t" + msg.toString());
        ByteBuffer msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log.info("rebuild\t" + ONExProtocolFactory.parser(msgBB).toString());
        log.info("after\t\t" + msg.getFlowMod());
        log.info("after\t\t" + msg.getOFPacketOut());

    }

    public static void ONExUploadLocalTopo(){

        ONExPacket msg = ONExProtocolFactory.ONExUploadLocalTopo("/tmp/some.file");
        log.info(msg);
        log.info(msg.getFileName());
        ByteBuffer msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        msg = ONExProtocolFactory.parser(msgBB);
        log.info(msg);
        log.info(msg.toString());
    }

    public static void ONExRequestGlobalTopo(){
        // 4  ONExRequestGlobalTopo
        ONExPacket msg = ONExProtocolFactory.ONExRequestGlobalTopo();
        log.info(msg.toString());
        ByteBuffer msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log.info(ONExProtocolFactory.parser(msgBB).toString());
    }


    /*
        deprecated
     */
//    public static void ONExResGlobalTopo(){
//        // 5 ONExResGlobalTopo
//        ONExPacket msg = ONExProtocolFactory.ONExResGlobalTopo(null);
//        log.info(msg.toString());
//        log.info(msg.getGlobalTopo());
//        ByteBuffer msgBB = ByteBuffer.allocate(msg.getLength());
//        msg.writeTo(msgBB);
//        msgBB.flip();
//        msg = ONExProtocolFactory.parser(msgBB);
//        log.info(msg.toString());
//        log.info(msg.getGlobalTopo());
//    }

    public static void ONExReqGlobalFlowMod(){
        // 6  ONExReqGlobalFlowMod
        OFFlowMod ofFlowMod = new OFFlowMod();
        ofFlowMod.setMatch(new OFMatch());

        GlobalFlowMod globalFlowMod = new GlobalFlowMod();
        globalFlowMod.addGlobalFlowModEntry(123L, ofFlowMod);

        ONExPacket msg = ONExProtocolFactory.ONExReqGlobalFlowMod(globalFlowMod);
        log.info(msg.toString());
        ByteBuffer msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        msg = ONExProtocolFactory.parser(msgBB);
        GlobalFlowMod g = msg.getGlobalFlowMod();
        log.info(msg.toString());
        log.info(g.toString());
    }

    public static void ONExSCFlowMod() {
        // 7  ONExSCFlowMod
        OFFlowMod ofFlowMod = new OFFlowMod();
        ofFlowMod.setMatch(new OFMatch());

        ONExPacket msg = ONExProtocolFactory.ONExSCFlowMod(
                ofFlowMod,
                1234567890L
        );
        log.info(msg.toString());
        ByteBuffer msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log.info(ONExProtocolFactory.parser(msgBB).toString());
    }

    public static void ln(){
        System.out.println("");
    }
}
