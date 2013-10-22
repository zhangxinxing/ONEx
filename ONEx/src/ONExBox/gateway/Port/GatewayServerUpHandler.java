/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package ONExBox.gateway.Port;

import ONExBox.BoxDaemon.BoxDaemon;
import ONExProtocol.ONExPacket;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.*;

public class GatewayServerUpHandler extends SimpleChannelHandler {

    private Logger log = Logger.getLogger(GatewayServerUpHandler.class);

    private BoxDaemon serverDaemon;

    public GatewayServerUpHandler(BoxDaemon serverDaemon){
        this.serverDaemon = serverDaemon;
    }

    @Override
    public void handleUpstream(
            ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
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
        ONExPacket op = (ONExPacket)e.getMessage();
        switch(op.getINS()){
            case ONExPacket.SPARE_PACKET_IN:
                // packet in from other server
                // should be sent to client
                log.debug("send to client daemon");
                serverDaemon.sendONEx(op);
                break;

            case ONExPacket.RES_SPARE_PACKET_IN:
                // from other server, stupid forward
                serverDaemon.sendONEx(op);
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
        log.error("Unexpected exception from downstream.", e.getCause());
        e.getChannel().close();
    }
}