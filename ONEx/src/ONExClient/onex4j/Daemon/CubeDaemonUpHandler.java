package ONExClient.onex4j.Daemon;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.*;

public class CubeDaemonUpHandler extends SimpleChannelUpstreamHandler {
    private static Logger log;

    public CubeDaemonUpHandler() {
        log = Logger.getLogger(CubeDaemonUpHandler.class);
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            log.trace(e.toString());
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        Channel chan = ctx.getChannel();
        log.debug("[client] connected with " + chan.getRemoteAddress().toString());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        // Send back the received message to the remote peer.
        log.debug("[client] Get message " + e.getMessage().toString());
        // TODO parse ONExProtocol here
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        // Close the connection when an exception is raised.
        log.error("Unexpected exception from downstream.", e.getCause());
        e.getChannel().close();
    }
}
