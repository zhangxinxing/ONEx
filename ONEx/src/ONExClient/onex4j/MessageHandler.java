package ONExClient.onex4j;

import ONExClient.onex4j.Interface.IPacketHandler;
import ONExClient.onex4j.SDKDaemon.SDKDaemon;
import ONExProtocol.ONExPacket;
import ONExProtocol.ONExProtocolFactory;
import org.apache.log4j.Logger;
import org.openflow.protocol.*;
import org.openflow.protocol.action.OFAction;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-10
 * Time: AM11:53
 */
public class MessageHandler implements IPacketHandler {
    private static Logger log = Logger.getLogger(MessageHandler.class);

    private ExecutorService exec;
    private SDKDaemon daemon;

    public MessageHandler(SDKDaemon daemon) {
        this.daemon = daemon;
        exec = Executors.newFixedThreadPool(10);
    }

    @Override
    public void onLocalPacketIn(OFMessage msg) {
        log.info(msg.toString());
    }

    @Override
    public void onRemotePacketIn(ONExPacket msg) {
        final InetSocketAddress src = msg.getSrcHost();
        assert src != null;
        if (msg.getINS() != ONExPacket.SPARE_PACKET_IN){
            log.error("Wrong type");
        }
        // 2
        exec.execute(new Runnable() {
            @Override
            public void run() {
                OFFlowMod ofFlowMod = new OFFlowMod();
                ofFlowMod.setMatch(new OFMatch());
                OFPacketOut po = new OFPacketOut();
                po.setActions(new LinkedList<OFAction>());

                ONExPacket res = ONExProtocolFactory.ONExResSparePI(ofFlowMod, po);
                res.setSrcHost(src);
                daemon.sendONEx(res);

                log.debug("send remote packet-in back to BoxDaemon");
            }
        });

    }

    @Override
    public void onRemotePacketOut(OFMessage msg) {
        //To change body of implemented methods use File | ONExSetting | File Templates.
    }
}
