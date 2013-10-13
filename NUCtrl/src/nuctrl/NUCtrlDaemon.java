package nuctrl;

import nuctrl.core.Core;
import nuctrl.core.MessageHandler;
import nuctrl.gateway.Gateway;
import nuctrl.interfaces.API;
import nuctrl.protocol.GatewayMsg;
import org.apache.log4j.Logger;

/**
 * User: fan
 * Date: 13-10-9
 * Time: PM3:51
 */
public class NUCtrlDaemon implements API {
    // exposed API

    private Core core;
    private static Logger log = Logger.getLogger(NUCtrlDaemon.class);

    public NUCtrlDaemon(MessageHandler msgHandler) {
        Settings.getInstance();

        if (msgHandler == null){
            log.error("Null msgHandler is not allowed");
        }

        Gateway gateway = new Gateway(msgHandler);
        core = new Core(gateway, msgHandler);
        // init
//        try {
//            Monitor.getInstance().overview();
//        } catch (SigarException e) {
//            e.printStackTrace();
//        }
    }

    public void halfShutdown(){
        core.halfShutdown();
    }

    public void fullShutdown(){
        log.debug("shut down everything");
        core.fullShutdown();
    }

    public void daemonOnPacket(GatewayMsg msg){
       core.dispatchFunc(msg);
    }


}
