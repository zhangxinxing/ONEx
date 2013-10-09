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
package nuctrl.gateway.Port;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class IOClient {

    private final String host;
    private final int port;
    private Channel channel;
    private ClientBootstrap bootstrap;

    public IOClient(String host, int port) {
        this.host = host;
        this.port = port;
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
                p.addLast("UpHandler", new ClientUpHandler());

                // downward
                p.addLast("DownHander", new ClientDownHandler());
                p.addLast("ObjectEncoder", new ObjectEncoder());
                return p;
            }
        });

        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
        channel = future.awaitUninterruptibly().getChannel();

        if (!future.isSuccess()) {
            future.getCause().printStackTrace();
            bootstrap.releaseExternalResources();
            return;
        }
    }

    public void send(Object obj){
        ChannelFuture future = channel.write(obj);
        future.awaitUninterruptibly();
    }

    private void destroy(){
        channel.close().awaitUninterruptibly();
        bootstrap.releaseExternalResources();
    }
}
