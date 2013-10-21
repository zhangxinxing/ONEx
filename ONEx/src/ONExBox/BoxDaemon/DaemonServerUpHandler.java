package ONExBox.BoxDaemon;

import ONExBox.gateway.Gateway;
import ONExProtocol.ONExPacket;
import ONExProtocol.ONExProtocolFactory;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;

public class DaemonServerUpHandler extends SimpleChannelHandler {

    private Logger log = Logger.getLogger(DaemonServerUpHandler.class);
    private Gateway gateway;
    private ONExServerDaemon serverDaemon;

    public DaemonServerUpHandler(Gateway gateway, ONExServerDaemon serverDaemon) {
        this.gateway = gateway;
        this.serverDaemon = serverDaemon;
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent &&
                ((ChannelStateEvent) e).getState() != ChannelState.INTEREST_OPS) {
            log.trace(e.toString());
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        // Send back the received message to the remote peer.
        log.debug("[server] Got message " + e.getMessage().toString());
        ONExPacket op = ONExProtocolFactory.parser(((ChannelBuffer) e.getMessage()).array());

        switch(op.getINS()){
            case ONExPacket.SPARE_PACKET_IN:
                log.debug("send to gateway spare");

                gateway.sparePacketIn(op);
                break;

            case ONExPacket.RES_SPARE_PACKET_IN:
                // from client
                // should be forward to its original place
                gateway.sendBackPacketIn(op);
                break;

            case ONExPacket.UPLOAD_LOCAL_TOPO:
                // TODO handle topology stuff
                break;

            case ONExPacket.GET_GLOBAL_TOPO:
                // TODO handle topology stuff
                break;

            case ONExPacket.REQ_GLOBAL_FLOW_MOD:
                // TODO handle topology stuff
                break;

            default:
                log.error("should not be received on client");
        }

        log.info(op.toString());
        log.debug("[server] Echo sent");
        ctx.getChannel().write(e.getMessage());

        try {
            super.messageReceived(ctx, e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        // Close the connection when an exception is raised.
        log.error("Exception from downstream.", e.getCause());
        e.getChannel().close();
    }
}