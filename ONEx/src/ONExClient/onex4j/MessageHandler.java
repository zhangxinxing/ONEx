package ONExClient.onex4j;

import ONExClient.onex4j.SDKDaemon.SDKDaemon;
import ONExProtocol.ONExPacket;
import org.apache.log4j.Logger;
import org.openflow.protocol.OFMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-10
 * Time: AM11:53
 */
public abstract class MessageHandler {
    private static Logger log = Logger.getLogger(MessageHandler.class);
    private ExecutorService exec;
    private SDKDaemon daemon;

    public MessageHandler() {
        exec = Executors.newFixedThreadPool(10);
    }

    public void setDaemon(SDKDaemon daemon){
        this.daemon = daemon;
    }

    public abstract void onLocalPacketIn(OFMessage msg);

    public abstract void onRemotePacketIn(ONExPacket msg);
//    {
//        final InetSocketAddress src = msg.getSrcHost();
//        assert src != null;
//        if (msg.getINS() != ONExPacket.SPARE_PACKET_IN){
//            log.error("Wrong type");
//        }
//        // 2
//        exec.execute(new Runnable() {
//            @Override
//            public void run() {
//                OFFlowMod ofFlowMod = new OFFlowMod();
//                ofFlowMod.setMatch(new OFMatch());
//                OFPacketOut po = new OFPacketOut();
//                po.setActions(new LinkedList<OFAction>());
//
//                ONExPacket res = ONExProtocolFactory.ONExResSparePI(ofFlowMod, po);
//                res.setSrcHost(src);
//                daemon.sendONEx(res);
//
//                log.debug("send remote packet-in back to BoxDaemon");
//            }
//        });
//
//    }

    public abstract void onRemotePacketOut(OFMessage msg);
}
