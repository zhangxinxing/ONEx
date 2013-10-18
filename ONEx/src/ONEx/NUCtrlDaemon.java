package ONEx;

import ONExClient.Java.MessageHandler;
import ONEx.gateway.Gateway;
import ONEx.protocol.GatewayMsg;
import org.apache.log4j.Logger;

/**
 * User: fan
 * Date: 13-10-9
 * Time: PM3:51
 */
public class NUCtrlDaemon {
    // exposed API

    private static Logger log = Logger.getLogger(NUCtrlDaemon.class);

    public NUCtrlDaemon() {

        // TODO should be added back
    }

    public void halfShutdown(){
    }

    public void fullShutdown(){
        log.debug("shut down everything");
    }

    public void daemonOnPacket(GatewayMsg msg){
    }


}
