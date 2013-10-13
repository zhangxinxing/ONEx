package nuctrl;

import nuctrl.core.Core;
import nuctrl.core.MessageHandler;
import nuctrl.gateway.Gateway;
import nuctrl.interfaces.API;
import nuctrl.interfaces.PacketHandler;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.MessageType;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.MessageEvent;

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

    public void onPacketIn(GatewayMsg msg){
       core.dispatchFunc(msg);
    }


}
