package ONExBox.BoxDaemon;

import ONExBox.gateway.Gateway;
import ONExProtocol.ONExPacket;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;

public class BoxDaemon {
    private static Logger log = Logger.getLogger(BoxDaemon.class);
    private ServerBootstrap bootstrap;
    private Channel clientChannel;

    public BoxDaemon(Gateway gateway, int port) {
        // Configure the server.
        bootstrap = new ServerBootstrap(
                new OioServerSocketChannelFactory(
                        Executors.newSingleThreadExecutor(),
                        Executors.newSingleThreadExecutor()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new BoxPipelineFactory(gateway, this));

        // Bind and start to accept incoming connections.
        Channel serverChannel = bootstrap.bind(new InetSocketAddress(port));
        if (clientChannel != null && !clientChannel.isBound()) {
            clientChannel.close().awaitUninterruptibly();
        }
        log.info("[BoxDaemon] bind: " + serverChannel.toString());
    }

    public void setClientChannel(Channel channel) {
        assert channel != null;
        this.clientChannel = channel;
        log.debug("ClientChannel set, from: " + channel.getRemoteAddress());
    }

    public void sendONEx(ONExPacket op) {
        log.debug("Sending: " + op);
        ByteBuffer buf = ByteBuffer.allocate(op.getLength());
        op.writeTo(buf);
        sendRaw(buf.array());
    }

    public void sendRaw(byte[] array) {
        assert clientChannel != null;
        ChannelBuffer buf = ChannelBuffers.copiedBuffer(array);
        ChannelFuture cf = clientChannel.write(buf);
        cf.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture cf) throws Exception {
                if (cf.isSuccess()) {
                    log.debug("sent to SDK: " +
                            clientChannel.getRemoteAddress().toString());
                } else {
                    log.error("Failed to send to SDK");
                    destroy();
                }
            }
        });

    }


    public void destroy() {
        clientChannel.close();
        bootstrap.releaseExternalResources();
    }
}

