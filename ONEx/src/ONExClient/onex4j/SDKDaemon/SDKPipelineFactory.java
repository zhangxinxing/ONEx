package ONExClient.onex4j.SDKDaemon;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

/**
 * Created with IntelliJ IDEA.
 * User: zhangf
 * Date: 13-10-22
 * Time: 上午10:57
 */
class SDKPipelineFactory implements ChannelPipelineFactory {
    SDKDaemon daemon;

    SDKPipelineFactory(SDKDaemon daemon) {
        this.daemon = daemon;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline p = Channels.pipeline();
        // upward
        p.addLast("UpHandler", new SDKUpHandler(daemon));
        // downward
        return p;
    }
}
