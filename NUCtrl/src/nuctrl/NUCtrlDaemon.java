package nuctrl;

import nuctrl.core.Core;
import nuctrl.core.MessageHandler;
import nuctrl.gateway.Gateway;
import nuctrl.interfaces.API;
import nuctrl.interfaces.PacketWorker;
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
    private Gateway gateway;
    private static Logger log = Logger.getLogger(NUCtrlDaemon.class);

    public NUCtrlDaemon(MessageHandler msgHandler) {
        Settings.getInstance();
        if (msgHandler == null){
            log.error("Null msgHandler is not allowed");
        }
        gateway = new Gateway(msgHandler);
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

    public static void main(String[] args){
        PacketWorker packetWorker = new PacketWorker() {
            @Override
            public void onPacket(GatewayMsg msg) {
                // hello here
                if (msg.getType() == MessageType.PACKET_IN.getType()){

                    if (msg.getEvent() != null){
                        log.info("Handle remote Packet-In");
                        MessageEvent event = msg.getEvent();
                        log.debug("From " + event.getChannel().getRemoteAddress().toString());

                        // TODO handling business here
                        GatewayMsg res = new GatewayMsg((byte)1, Settings.getInstance().socketAddr);
                        ChannelFuture write = event.getChannel().write(res);
                        write.awaitUninterruptibly();
                        if (write.isSuccess()){
                            log.debug("Packet-Out send out");
                        }
                    }
                    else {
                        log.info("Local Packet-In");
                        // TODO handling packet-in

                    }
                } // end of if packet-in

                else if (msg.getType() == MessageType.PACKET_OUT.getType()){
                    log.info("handler packet out");
                }
            }
        };

        /* == main == */
        NUCtrlDaemon daemon = new NUCtrlDaemon(new MessageHandler(packetWorker));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GatewayMsg msg = new GatewayMsg((byte)0, Settings.getInstance().socketAddr);
        int TEST = 1;
        for (int i = 0; i < TEST; i++){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                break;
            }
            daemon.onPacketIn(msg);
        }
    }
}
