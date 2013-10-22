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

import ONExProtocol.ONExPacket;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
public class IOClient {


    private Channel channel;
    private ClientBootstrap bootstrap;
    private static Logger log = Logger.getLogger(IOClient.class);

    public IOClient(InetSocketAddress address) {

        bootstrap = new ClientBootstrap();
        bootstrap.setFactory(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool())
        );

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline p = Channels.pipeline();

                // upward
                p.addLast("ObjectDecoder", new ObjectDecoder(
                        ClassResolvers.cacheDisabled(
                                getClass().getClassLoader())
                ));
                p.addLast("UpHandler", new GatewayClientUpHandler());

                // downward
                p.addLast("ObjectEncoder", new ObjectEncoder());
                return p;
            }
        });

        ChannelFuture future = bootstrap.connect(address);
        channel = future.getChannel();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    log.error(future.getCause());
                    bootstrap.releaseExternalResources();
                }
            }
        });
    }

    public void send(ONExPacket msg){
        if(!channel.isWritable()){
            log.error("[Gateway, Client]Channel not writable");
            System.exit(-1);
        }
        ChannelFuture future = channel.write(msg);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (!channelFuture.isSuccess()){
                    log.error("[Gateway Client] sending fails");
                }
            }
        });
    }

    public void destroy(){
        channel.close().awaitUninterruptibly();
        bootstrap.releaseExternalResources();
    }
}
