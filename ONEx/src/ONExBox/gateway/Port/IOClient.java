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

import ONExClient.Java.MessageHandler;
import ONExBox.protocol.GatewayMsg;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class IOClient {

    private InetSocketAddress address;
    private Channel channel;
    private ClientBootstrap bootstrap;
    private MessageHandler msgHandler;

    public IOClient(InetSocketAddress address, MessageHandler msgHandler) {
        this.address = address;
        this.msgHandler = msgHandler;
    }

    public void init() {
        // Configure the client.
        bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline p = Channels.pipeline();

                // upward
                p.addLast("ObjectDecoder", new ObjectDecoder(
                        ClassResolvers.cacheDisabled(
                                getClass().getClassLoader())
                ));
                p.addLast("UpHandler", new ClientUpHandler(msgHandler));

                // downward
                p.addLast("DownHander", new ClientDownHandler());
                p.addLast("ObjectEncoder", new ObjectEncoder());
                return p;
            }
        });

        ChannelFuture future = bootstrap.connect(address);
        channel = future.awaitUninterruptibly().getChannel();

        if (!future.isSuccess()) {
            future.getCause().printStackTrace();
            bootstrap.releaseExternalResources();
        }
    }

    public void send(GatewayMsg msg){
        ChannelFuture future = channel.write(msg);
        future.awaitUninterruptibly();
    }

    public void destroy(){
        channel.close().awaitUninterruptibly();
        bootstrap.releaseExternalResources();
    }
}
