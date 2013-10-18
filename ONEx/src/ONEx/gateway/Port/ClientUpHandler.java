package ONEx.gateway.Port;

import ONEx.core.MessageHandler;
import ONEx.gateway.gatewayDispatcher;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.*;

public class ClientUpHandler extends SimpleChannelUpstreamHandler {
    private static Logger log;
    private gatewayDispatcher dispatcher;

    public ClientUpHandler(MessageHandler msgHandler) {
        log = Logger.getLogger(ClientUpHandler.class);
        this.dispatcher = new gatewayDispatcher(msgHandler);
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

        dispatcher.dispatchFunc(e);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        // Close the connection when an exception is raised.
        log.error("Unexpected exception from downstream.", e.getCause());
        e.getChannel().close();
    }
}
