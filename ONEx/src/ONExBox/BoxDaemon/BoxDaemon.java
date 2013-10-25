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
package ONExBox.BoxDaemon;

import ONExBox.gateway.Gateway;
import ONExProtocol.GlobalTopo;
import ONExProtocol.ONExPacket;
import ONExProtocol.ONExProtocolFactory;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;

public class BoxDaemon {
    private static Logger log = Logger.getLogger(BoxDaemon.class);
    private ServerBootstrap bootstrap;
    private Channel clientChannel;

    public BoxDaemon(Gateway gateway, int port){
        // Configure the server.
        bootstrap = new ServerBootstrap(
                new OioServerSocketChannelFactory(
                        Executors.newSingleThreadExecutor(),
                        Executors.newSingleThreadExecutor()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new BoxPipelineFactory(gateway, this));

        // Bind and start to accept incoming connections.
        Channel serverChannel = bootstrap.bind(new InetSocketAddress(port));
        if (clientChannel != null && !clientChannel.isBound()){
            clientChannel.close().awaitUninterruptibly();
        }
        log.info("[BoxDaemon] bind: " + serverChannel.toString());
    }

    public void setClientChannel(Channel channel){
        assert channel != null;
        this.clientChannel = channel;
        log.debug("ClientChannel set, from: " + channel.getLocalAddress().toString());
    }

    public void sendONEx(ONExPacket op){
        ByteBuffer buf = ByteBuffer.allocate(op.getLength());
        op.writeTo(buf);
        sendRaw(buf.array());
    }

    public void sendRaw(byte[] array){
        assert clientChannel != null;
        ChannelBuffer buf = ChannelBuffers.copiedBuffer(array);
        ChannelFuture cf= clientChannel.write(buf);
        cf.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture cf) throws Exception {
                if (!cf.isSuccess()){
                    log.error("Failed to send to SDK");
                    destroy();
                }
                else{
                    log.debug("sent to SDK");
                }
            }
        });

    }

    public void returnGlobalTopo(GlobalTopo topo){
        log.debug("return global topo");
        ONExPacket op = ONExProtocolFactory.ONExResGlobalTopo(topo);
        sendONEx(op);
    }


    public void destroy(){
        clientChannel.close();
        bootstrap.releaseExternalResources();
    }
}

