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

import ONExProtocol.ONExPacket;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;

public class ONExServerDaemon {
    private static Logger log = Logger.getLogger(ONExServerDaemon.class);
    private ServerBootstrap bootstrap;
    private Channel clientChannel;

    public ONExServerDaemon(int port){
        // Configure the server.
        bootstrap = new ServerBootstrap(
                new OioServerSocketChannelFactory(
                        Executors.newSingleThreadExecutor(),
                        Executors.newSingleThreadExecutor()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new IOServerPipelineFactory());

        // Bind and start to accept incoming connections.
        clientChannel = bootstrap.bind(new InetSocketAddress(port));
        log.debug("Bind to " + port);
    }

    public void sendONEx(ONExPacket ONExP){
        ByteBuffer buf = ByteBuffer.allocate(ONExP.getLength());
        sendRaw(buf.array());
    }

    public void sendRaw(byte[] array){
        ChannelBuffer buf = ChannelBuffers.copiedBuffer(array);
        ChannelFuture cf= clientChannel.write(buf);
        cf.awaitUninterruptibly();
        if (!cf.isSuccess()){
            log.error("Failed to send " + buf.toString());
            destroy();
        }
        else{
            log.debug(buf.toString() + " sent");
        }
    }


    public void destroy(){
        bootstrap.releaseExternalResources();
    }
}

class IOServerPipelineFactory implements ChannelPipelineFactory{
    public IOServerPipelineFactory() {

    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline p = Channels.pipeline();
        // upward
        p.addLast("UpHandler", new ServerUpHandler());
        // downward
        return p;
    }
}
