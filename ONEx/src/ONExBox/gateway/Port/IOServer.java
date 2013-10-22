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
import ONExBox.ONExSetting;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Echoes back any received data from a client.
 */
public class IOServer {

    private int port;
    private static Logger log = Logger.getLogger(IOServer.class);
    private ServerBootstrap bootstrap;

    public IOServer(BoxDaemon serverDaemon){
        this.port = ONExSetting.PORT;
        // Configure the server.
        bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new IOServerPipelineFactory(serverDaemon));

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(port));
    }

    public void destroy(){
        bootstrap.releaseExternalResources();
    }
}

class IOServerPipelineFactory implements ChannelPipelineFactory{

    BoxDaemon serverDaemon;
    public IOServerPipelineFactory(BoxDaemon serverDaemon) {
        this.serverDaemon = serverDaemon;

    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline p = Channels.pipeline();
        // upward
        p.addLast("Decoder", new ObjectDecoder(
                ClassResolvers.cacheDisabled(
                        getClass().getClassLoader())
        ));
        p.addLast("UpHandler", new GatewayServerUpHandler(serverDaemon));

        // downward
        p.addLast("Encoder", new ObjectEncoder());

        return p;
    }
}
