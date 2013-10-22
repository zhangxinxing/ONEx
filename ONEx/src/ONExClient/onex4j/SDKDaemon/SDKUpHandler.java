package ONExClient.onex4j.SDKDaemon;

import MyDebugger.Dumper;
import ONExProtocol.ONExPacket;
import ONExProtocol.ONExProtocolFactory;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;

class SDKUpHandler extends SimpleChannelUpstreamHandler {
    private static Logger log;
    private SDKDaemon daemon;

    public SDKUpHandler(SDKDaemon daemon) {
        log = Logger.getLogger(SDKUpHandler.class);
        this.daemon = daemon;
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
        log.debug("[SDKDaemon] connected with " + chan.getRemoteAddress().toString()
                + " at: " + chan.getLocalAddress().toString());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        // Send back the received message to the remote peer.
        ChannelBuffer buf = (ChannelBuffer)e.getMessage();
        ONExPacket op = ONExProtocolFactory.parser(buf.array());
        log.debug("Get message " + op.toString());
        daemon.dispatchONEx(op);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e){
        // Close the connection when an exception is raised.
        log.error("Exception:" + e.getCause());
        e.getChannel().close();
    }
}
