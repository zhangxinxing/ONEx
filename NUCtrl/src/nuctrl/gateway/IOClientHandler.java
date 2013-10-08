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
package nuctrl.gateway;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;

import org.apache.log4j.Logger;

/**
 * Handler implementation for the echo client.  It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class IOClientHandler extends SimpleChannelUpstreamHandler {
    private Logger log = Logger.getLogger(IOClientHandler.class);

    public IOClientHandler() {

    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        Channel chan = ctx.getChannel();
        log.info("[client] connected with " + chan.toString());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        // Send back the received message to the remote peer.
        log.info("[client] Get message" + e.getMessage().toString());
        e.getChannel().write(e.getMessage());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        // Close the connection when an exception is raised.
        log.error("Unexpected exception from downstream.", e.getCause());
        e.getChannel().close();
    }
}
