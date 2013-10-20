package ONExClient.Java.Daemon;

import ONExClient.Java.Interface.IONExDaemon;
import ONExClient.Java.MessageHandler;
import ONExProtocol.ONExPacket;
import ONExClient.Java.SwitchDealer;
import ONExClient.Java.TopologyDealer;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.openflow.protocol.OFMessage;

import java.net.ConnectException;
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
    private static final int port = 12345;
    private Channel channel;
    private ClientBootstrap bootstrap;

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

        // Configure the client.
        bootstrap = new ClientBootstrap(
                new OioClientSocketChannelFactory(
                        Executors.newSingleThreadExecutor(),
                        ThreadNameDeterminer.CURRENT)
        );

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new pipelineFactory());
        // connect
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
        channel = future.awaitUninterruptibly().getChannel();

        if (!future.isSuccess()) {
            destroy();
            return;
        }
        log.debug(String.format("connected with %s:%d", host, port));
    }

    @Override
    public void sendONEx(ONExPacket OP) {
        sendRaw(OP.toByteBuffer().array());
    }

    public void sendRaw(byte[] array){
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
    public void parseONEx() {
        int instruction = 0;
        OFMessage msg = null;

        switch(instruction){
            case ONExPacket.SPARE_PACKET_IN:
                messageHandler.onRemotePacketIn(msg);
                break;

            case ONExPacket.RES_SPARE_PACKET_IN:
                messageHandler.onRemotePacketOut(msg);
                break;

            case ONExPacket.RES_GET_GLOBAL_TOPO:
                topologyDealer.parseGlobalTopo();
                break;

            case ONExPacket.SC_FLOW_MOD:
                switchDealer.sendFlowMod();
                break;

            default:
                System.err.println("should not be received on client");
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

class pipelineFactory implements ChannelPipelineFactory{

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline p = Channels.pipeline();
        // upward
        p.addLast("UpHandler", new ClientUpHandler());
        // downward
        return p;
    }
}