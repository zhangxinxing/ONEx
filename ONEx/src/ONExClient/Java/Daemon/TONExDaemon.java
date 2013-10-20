package ONExClient.Java.Daemon;

import ONExClient.Java.MessageHandler;
import ONExClient.Java.SwitchDealer;
import ONExClient.Java.TopologyDealer;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM12:41
 */
public class TONExDaemon {
    public static void main(String[] args) {
        ONExDaemon daemon = new ONExDaemon(
                new MessageHandler(),
                new TopologyDealer(new SwitchDealer()),
                new SwitchDealer()
        );

        daemon.send(new byte[]{1,2,3});

    }
}
