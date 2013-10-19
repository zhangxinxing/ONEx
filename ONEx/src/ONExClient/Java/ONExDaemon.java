package ONExClient.Java;

import ONExClient.Java.Interface.IONExDaemon;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.openflow.protocol.OFMessage;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-18
 * Time: PM11:47
 */
public class ONExDaemon implements IONExDaemon {
    private static final String host = "127.0.0.1";
    private static final int port = 9999;

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
        // Configure the client.
        ClientBootstrap bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(

                        new ONExClientHandler());
            }
        });

        // Start the connection attempt.
        bootstrap.connect(new InetSocketAddress(host, port));
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

            case ONExProtocol.RES_SPARE_PACKET_IN:
                messageHandler.onRemotePacketOut(msg);
                break;

            case ONExProtocol.RES_GET_GLOBAL_TOPO:
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
