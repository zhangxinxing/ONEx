package nuctrl.gateway.Port;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.*;

/**
 * Handler implementation for the echo client.  It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class ClientUpHandler extends SimpleChannelUpstreamHandler {
    private Logger log;
    private gatewayDispatcher dispatcher;

    public ClientUpHandler() {
        log = Logger.getLogger(ClientUpHandler.class);
        this.dispatcher = new gatewayDispatcher();
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
        log.info("[client] connected with " + chan.getRemoteAddress().toString());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        // Send back the received message to the remote peer.
        log.info("[client] Get message " + e.getMessage().toString());

        dispatcher.dispatchFunc(e);


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        // Close the connection when an exception is raised.
        log.error("Unexpected exception from downstream.", e.getCause());
        e.getChannel().close();
    }
}
