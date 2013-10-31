package ONExBox.BoxDaemon;

import ONExBox.gateway.Gateway;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;

/**
 * Created with IntelliJ IDEA.
 * User: zhangf
 * Date: 13-10-22
 * Time: 上午10:43
 */
class BoxPipelineFactory implements ChannelPipelineFactory {
    private Gateway gateway;
    private BoxDaemon serverDaemon;
    public BoxPipelineFactory(Gateway gateway, BoxDaemon serverDaemon) {
        this.gateway = gateway;
        this.serverDaemon = serverDaemon;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline p = Channels.pipeline();
        // upward
        p.addLast("framing", new LengthFieldBasedFrameDecoder(5120, 1, 4, -5 ,0));
        p.addLast("UpHandler", new BoxUpHandler(gateway, serverDaemon));
        // downward
        return p;
    }
}
