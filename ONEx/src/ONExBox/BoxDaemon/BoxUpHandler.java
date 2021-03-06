package ONExBox.BoxDaemon;

import ONExBox.ONExSetting;
import ONExBox.gateway.Gateway;
import ONExProtocol.ONExPacket;
import ONExProtocol.ONExProtocolFactory;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;

class BoxUpHandler extends SimpleChannelHandler {

    private Logger log = Logger.getLogger(BoxUpHandler.class);
    private Gateway gateway;
    private BoxDaemon boxDaemon;

    public BoxUpHandler(Gateway gateway, BoxDaemon boxDaemon) {
        this.gateway = gateway;
        this.boxDaemon = boxDaemon;
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent &&
                ((ChannelStateEvent) e).getState() != ChannelState.INTEREST_OPS) {
            log.trace(e.getChannel().toString() + ":" + ((ChannelStateEvent) e).getState());
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        Channel chan = ctx.getChannel();
        boxDaemon.setClientChannel(chan);
        log.info("connected with " + chan.getRemoteAddress());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        ONExPacket op = ONExProtocolFactory.parser(((ChannelBuffer) e.getMessage()).array());
        if (!op.isValid()) {
            log.error("Error in parsing wired ONExProtocol");
            return;
        }
        gateway.setControllerID(op.getControllerID());
        switch (op.getINS()) {
            case ONExPacket.SPARE_PACKET_IN:
                log.debug("send to gateway SPARE_PACKET_IN");
                op.setSrcHost(ONExSetting.getInstance().socketAddr);
                gateway.sparePacketIn(op);
                break;

            case ONExPacket.RES_SPARE_PACKET_IN:
                // from client
                // should be forward to its original place
                log.debug("send to gateway res_spare");
                gateway.sendBackPacketIn(op);
                break;

            case ONExPacket.UPLOAD_LOCAL_TOPO:
                // from SDK
                // no reply
                String fileName = op.getFileName();
                log.debug("get UPLOAD_LOCAL_TOPO, DB name: " + fileName);
                gateway.submitTopology(fileName);
                break;

            case ONExPacket.REQUEST_GLOBAL_TOPO:
                break;

            case ONExPacket.REQ_GLOBAL_FLOW_MOD:
                log.debug(op.getGlobalFlowMod().toString());
                break;

            default:
                log.error("should not be received on client");
        }

        try {
            super.messageReceived(ctx, e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        // Close the connection when an exception is raised.
        log.error("Exception:");
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
}