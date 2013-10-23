package ONExClient.onex4j.SDKDaemon;

import ONExClient.onex4j.Interface.IONExDaemon;
import ONExClient.onex4j.MessageHandler;
import ONExProtocol.ONExPacket;
import ONExClient.onex4j.SwitchDealer;
import ONExClient.onex4j.TopologyDealer;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketOut;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-18
 * Time: PM11:47
 */
public class SDKDaemon implements IONExDaemon {
    private static final String host = "localhost";
    private Channel channel;
    private ClientBootstrap bootstrap;

    private MessageHandler messageHandler;
    private TopologyDealer topologyDealer;
    private SwitchDealer switchDealer;
    private Logger log = Logger.getLogger(SDKDaemon.class);

    public SDKDaemon(int port, TopologyDealer topo_h, SwitchDealer sw_h) {
        if (topo_h == null || sw_h == null){
            log.error("Null arguments");
        }
        this.messageHandler = new MessageHandler(this);
        this.topologyDealer = topo_h;
        this.switchDealer = sw_h;

        // Configure the client.
        bootstrap = new ClientBootstrap();
        bootstrap.setFactory(
                new OioClientSocketChannelFactory(
                        Executors.newSingleThreadExecutor()
                )
        );

        bootstrap.setPipelineFactory(new SDKPipelineFactory(this));
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
        channel = future.awaitUninterruptibly().getChannel();

        log.debug(String.format("connected with %s:%d", host, port));
    }

    @Override
    public void sendONEx(ONExPacket op) {
        log.debug("send: " + op.toString());
        sendRaw(op.toByteBuffer().array());
    }

    private void sendRaw(byte[] array){
        ChannelFuture future = channel.write(ChannelBuffers.copiedBuffer(array));
        future.awaitUninterruptibly();
    }

    @Override
    public void sparePacketIn(OFMessage msg) {
        ByteBuffer MsgBB = ByteBuffer.allocate(msg.getLengthU());
        log.info(MsgBB.toString());
        msg.writeTo(MsgBB);
        log.info(MsgBB.toString());
    }

    @Override
    public void dispatchONEx(ONExPacket op) {
        switch(op.getINS()){
            case ONExPacket.SPARE_PACKET_IN:
                log.debug("dispatching SPARE_PACKET_IN");
                messageHandler.onRemotePacketIn(op);
                break;

            case ONExPacket.RES_SPARE_PACKET_IN:
                // TODO flowmod and packet-out
                OFFlowMod ofFlowMod = op.getFlowMod();
                OFPacketOut ofPacketOut = op.getOFPacketOut();

                assert ofPacketOut != null;
                assert ofFlowMod != null;
                log.debug(" get ofFlowMod " + ofFlowMod.toString());
                log.debug(" and ofPacketOut " + ofPacketOut.toString());
                if (op.getFlowMod() != null){
                    switchDealer.sendFlowMod(ofFlowMod);
                }
                messageHandler.onRemotePacketOut(ofPacketOut);
                break;

            case ONExPacket.UPLOAD_LOCAL_TOPO:
                log.error("Wrong type");
                break;

            case ONExPacket.GET_GLOBAL_TOPO:
                log.error("Wrong type");
                break;

            case ONExPacket.RETURN_GLOBAL_TOPO:
                topologyDealer.parseGlobalTopo();
                break;

            case ONExPacket.REQ_GLOBAL_FLOW_MOD:
                log.error("Wrong type");
                break;

            case ONExPacket.SC_FLOW_MOD:
                switchDealer.sendFlowMod(op.getFlowMod());
                break;

            default:
               log.error("should not be received on client");
        }
    }


    public void destroy(){
        if (channel != null)
            channel.close().awaitUninterruptibly();
        bootstrap.releaseExternalResources();
    }

    @Override
    public String toString(){
        return "Fucking";
    }
}

