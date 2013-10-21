package ONExClient.onex4j;

import ONExClient.onex4j.Interface.IPacketHandler;
import org.apache.log4j.Logger;
import org.openflow.protocol.OFMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-10
 * Time: AM11:53
 */
public class MessageHandler implements IPacketHandler {
    private static Logger log = Logger.getLogger(MessageHandler.class);

    private ExecutorService exec;


    public MessageHandler() {
        exec = Executors.newFixedThreadPool(10);
    }

    @Override
    public void onLocalPacketIn(OFMessage msg) {
        log.info(msg.toString());
    }

    @Override
    public void onRemotePacketIn(OFMessage msg) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onRemotePacketOut(OFMessage msg) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
