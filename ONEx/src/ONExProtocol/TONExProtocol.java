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

        TONExProtocol.ONExUploadLocalTopo();

        TONExProtocol.ONExRequestGlobalTopo();
        TONExProtocol.ONExResGlobalTopo();

        TONExProtocol.ONExReqGlobalFlowMod();
        TONExProtocol.ONExSCFlowMod();
    }

    public static void ONExSparePI(){
        byte[] ba = {0x01,0x01,0x01};
        OFPacketIn pi = new OFPacketIn();
        pi.setReason(OFPacketIn.OFPacketInReason.NO_MATCH);
        pi.setPacketData(ba);
        log.info("before:\t" + pi.toString());
        ONExPacket msg = ONExProtocolFactory.ONExSparePI(pi);
        msg.setSrcHost(new InetSocketAddress("127.1.2.3", 1234));
        log.info("build:\t" + msg.toString());

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

        ONExPacket msg = ONExProtocolFactory.ONExResSparePI(ofFlowMod, po);
        msg.setSrcHost(new InetSocketAddress("127.1.2.3", 1234));
        log.info("build\t\t" + msg.toString());
        ByteBuffer msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log.info("rebuild\t" + ONExProtocolFactory.parser(msgBB).toString());
        log.info("after\t\t" + msg.getFlowMod());
        log.info("after\t\t" + msg.getOFPacketOut());

    }

    public static void ONExUploadLocalTopo(){
        GlobalTopo globalTopo = new GlobalTopo();
        globalTopo.addHostEntry(
                12345L,
                (short) 123,
                1234,
                new byte[]{1,2,3,4,5,6}
        );

        globalTopo.addSwitchLink(
                1L,
                (short)1,
                2L,
                (short)2
        );
        ONExPacket msg = ONExProtocolFactory.ONExUploadLocalTopo(globalTopo);
//        log.info(msg);
//        log.info(msg.getGlobalTopo());
        ByteBuffer msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        msg = ONExProtocolFactory.parser(msgBB);
//        log.info(msg);
        globalTopo = msg.getGlobalTopo();
        log.info(globalTopo.toString());
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


    public static void ONExResGlobalTopo(){
        // 5 ONExResGlobalTopo
        GlobalTopo globalTopo = new GlobalTopo();
        globalTopo.addHostEntry(123L,(short)1, 123, new byte[] {1,2,3,4,5,6});
        globalTopo.addSwitchLink(123L, (short)123, 124L, (short)124);
        ONExPacket msg = ONExProtocolFactory.ONExResGlobalTopo(globalTopo);
        log.info(msg.toString());
        log.info(msg.getGlobalTopo());
        ByteBuffer msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        msg = ONExProtocolFactory.parser(msgBB);
        log.info(msg.toString());
        log.info(msg.getGlobalTopo());
    }

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

        ONExPacket msg = ONExProtocolFactory.ONExSCFlowMod(ofFlowMod);
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
