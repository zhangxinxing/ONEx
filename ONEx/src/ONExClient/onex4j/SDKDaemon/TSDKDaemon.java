package ONExClient.onex4j.SDKDaemon;

import ONExProtocol.GlobalFlowMod;
import ONExProtocol.LocalTopo;
import ONExClient.onex4j.SwitchDealer;
import ONExClient.onex4j.TopologyDealer;
import ONExProtocol.ONExPacket;
import ONExProtocol.ONExProtocolFactory;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM12:41
 */
public class TSDKDaemon {
    public static void main(String[] args) {
//        SDKDaemon daemon = new SDKDaemon(
//                12345,
//                new TopologyDealer(new SwitchDealer()),
//                new SwitchDealer()
//        );

//        byte[] ba = {0x01,0x01,0x01};
//        OFPacketIn pi = new OFPacketIn();
//        pi.setReason(OFPacketIn.OFPacketInReason.NO_MATCH);
//        pi.setPacketData(ba);
//        ONExPacket msg = ONExProtocolFactory.ONExSparePI(pi);
//
//        daemon.sendONEx(msg);

//        InetAddress addr = null;
//        try {
//            addr = InetAddress.getByAddress(new byte[]{56,57,58,59});
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//
//        LocalTopo localTopo = new LocalTopo();
//        localTopo.addOrUpdateSwitch(12345L);
//        localTopo.udpatePortInfo(
//                12345L,
//                (short)123,
//                LocalTopo.Status.HOST,
//                addr,
//                new byte[] {1,2,3,4,5,6}
//        );
//
//        ONExPacket msg = ONExProtocolFactory.ONExUploadLocalTopo(localTopo);
//        daemon.sendONEx(msg);

//        OFFlowMod ofFlowMod = new OFFlowMod();
//        ofFlowMod.setMatch(new OFMatch());
//
//        GlobalFlowMod globalFlowMod = new GlobalFlowMod();
//        globalFlowMod.addGlobalFlowModEntry(123L, ofFlowMod);
//
//        ONExPacket msg = ONExProtocolFactory.ONExReqGlobalFlowMod(globalFlowMod);
//        daemon.sendONEx(msg);

    }

    static void log(Object obj){
        System.out.println(obj);
    }
}
