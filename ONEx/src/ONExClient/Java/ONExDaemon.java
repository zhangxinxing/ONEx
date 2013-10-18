package ONExClient.Java;

import ONExClient.Java.Interface.IONExDaemon;
import org.openflow.protocol.OFMessage;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-18
 * Time: PM11:47
 */
public class ONExDaemon implements IONExDaemon {
    private MessageHandler messageHandler;
    private TopologyDealer topologyDealer;
    private SwitchDealer switchDealer;
    private Logger log = Logger.getLogger(ONExDaemon.class);

    public ONExDaemon(MessageHandler msg_h, TopologyDealer topo_h, SwitchDealer sw_h) {
        if (msg_h == null || topo_h == null || sw_h == null){
            log.error("Null arguments");
        }
        this.messageHandler = msg_h;
        this.topologyDealer = topo_h;
        this.switchDealer = sw_h;

        // TODO MAKE SOME SOCKETS HERE
    }

    @Override
    public void sendONEx(ONExProtocol OP) {

    }

    @Override
    public void sparePacketIn(OFMessage msg) {
        ByteBuffer MsgBB = ByteBuffer.allocate(msg.getLengthU());
        log.info(MsgBB.toString());
        msg.writeTo(MsgBB);
        log.info(MsgBB.toString());
    }

    @Override
    public void parseONEx() {
        int instruction = 0;
        OFMessage msg = null;

        switch(instruction){
            case ONExProtocol.SPARE_PACKET_IN:
                messageHandler.onRemotePacketIn(msg);
                break;

            case ONExProtocol.REP_SPARE_PACKET_IN:
                messageHandler.onRemotePacketOut(msg);
                break;

            case ONExProtocol.REP_GET_GLOBAL_TOPO:
                topologyDealer.parseGlobalTopo();
                break;

            case ONExProtocol.SC_FLOW_MOD:
                switchDealer.sendFlowMod();
                break;

            default:
                System.err.println("should not be received on client");
        }
    }

    @Override
    public void listenOn() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String toString(){
        return "Fucking";
    }
}
