package ONExProtocol;

import ONExClient.onex4j.GlobalTopo;
import org.openflow.protocol.*;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.factory.BasicFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
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

        // 1 ONExSparePI
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

        // 2 ONExResSparePI
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

        // 3 ONExUploadLocalTopo
        InetAddress addr = null;
        try {
            addr = InetAddress.getByAddress(new byte[]{56,57,58,59});
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        LocalTopo localTopo = new LocalTopo();
        localTopo.addOrUpdateSwitch(12345L);
        localTopo.udpatePortInfo(
                12345L,
                (short)123,
                LocalTopo.Status.HOST,
                addr,
                new byte[] {1,2,3,4,5,6}
        );
        localTopo.udpatePortInfo(
                12345L,
                (short)1243,
                LocalTopo.Status.HOST,
                addr,
                new byte[] {1,2,3,4,5,6}
        );

        msg = ONExProtocolFactory.ONExUploadLocalTopo(localTopo);
        log.info(msg);
        log.info(msg.getLocalTopo());
        msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        msg = ONExProtocolFactory.parser(msgBB);
        log.info(msg);
        log.info(msg.getLocalTopo());

        // 4  ONExRequestGlobalTopo
        msg = ONExProtocolFactory.ONExRequestGlobalTopo();
        log.info(msg.toString());
        msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log.info(ONExProtocolFactory.parser(msgBB).toString());

        // 5 ONExResGlobalTopo
        GlobalTopo globalTopo = new GlobalTopo();
        globalTopo.addHostEntry(123L,(short)1, 123, new byte[] {1,2,3,4,5,6});
        globalTopo.addSwitchLink(123L, (short)123, 124L, (short)124);
        msg = ONExProtocolFactory.ONExResGlobalTopo(globalTopo);
        log.info(msg.toString());
        log.info(msg.getGlobalTopo());
        msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        msg = ONExProtocolFactory.parser(msgBB);
        log.info(msg.toString());
        log.info(msg.getGlobalTopo());

        // 6  ONExReqGlobalFlowMod
        ofFlowMod = new OFFlowMod();
        ofFlowMod.setMatch(new OFMatch());

        GlobalFlowMod globalFlowMod = new GlobalFlowMod();
        globalFlowMod.addGlobalFlowModEntry(123L, ofFlowMod);

        msg = ONExProtocolFactory.ONExReqGlobalFlowMod(globalFlowMod);
        log.info(msg.toString());
        msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        msg = ONExProtocolFactory.parser(msgBB);
        GlobalFlowMod g = msg.getGlobalFlowMod();
        log.info(msg.toString());
        log.info(g.toString());

        // 7  ONExSCFlowMod
        msg = ONExProtocolFactory.ONExSCFlowMod(ofFlowMod);
        log.info(msg.toString());
        msgBB = ByteBuffer.allocate(msg.getLength());
        msg.writeTo(msgBB);
        msgBB.flip();
        log.info(ONExProtocolFactory.parser(msgBB).toString());
    }
}
