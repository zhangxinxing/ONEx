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
package ONEx.gateway.Port;

import ONEx.Settings;
import ONExClient.Java.PacketHandler.MessageHandler;
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

    private final int port;
    private static Logger log = Logger.getLogger(IOServer.class);
    private MessageHandler messageHandler;
    private ServerBootstrap bootstrap;

    public IOServer(MessageHandler msgHandler){
        this.port = Settings.PORT;
        if (msgHandler != null){
            this.messageHandler = msgHandler;
        }
        else{
            log.error("Null past to constructor");
            System.exit(-1);
        }
    }

    public void init() {
        // Configure the server.
        bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new IOServerPipelineFactory(messageHandler));

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(port));
    }

    public void destroy(){
        bootstrap.releaseExternalResources();
    }
}

class IOServerPipelineFactory implements ChannelPipelineFactory{
    private MessageHandler messageHandler;

    public IOServerPipelineFactory(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline p = Channels.pipeline();
        // upward
        p.addLast("Decoder", new ObjectDecoder(
                ClassResolvers.cacheDisabled(
                        getClass().getClassLoader())
        ));
        p.addLast("UpHandler", new ServerUpHandler(messageHandler));

        // downward
        p.addLast("DownHandler", new ServerDownHandler());
        p.addLast("Encoder", new ObjectEncoder());

        return p;
    }
}
