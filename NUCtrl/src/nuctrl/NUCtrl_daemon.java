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
public class NUCtrl_daemon implements API {
    // exposed API

    private Core core;
    private Gateway gateway;
    private static Logger log = Logger.getLogger(NUCtrl_daemon.class);

    public NUCtrl_daemon(MessageHandler msgHandler) {
        if (msgHandler == null){
            log.error("Null msgHandler is not allowed");
        }
        gateway = new Gateway(msgHandler);
        core = new Core(gateway, msgHandler);
        Settings.getInstance();
    }

    // setup
    public void run(){
        gateway.setup();
        initUpdateGlobalInfo();
    }

    // updateGlobalInfo
    private void initUpdateGlobalInfo(){
        // a thread for updating busy table regularly
        Thread updateBusyTable = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(Settings.BUSY_UPDATE_INT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    // every 1s
                    gateway.getDataSharing().updateBusyTable();
                }
            }
        });

        updateBusyTable.start();
    }

    public void onPacketIn(GatewayMsg msg){
       core.dispatchFunc(msg);
    }

    public static void main(String[] args){
        PacketWorker packetWorker = new PacketWorker() {
            @Override
            public void onpPacket(GatewayMsg msg) {
                // hello here
                if (msg.getType() == MessageType.PACKET_IN.getType()){
                    log.info("handle packet in");

                    if (msg.getEvent() != null){
                        MessageEvent event = msg.getEvent();

                        log.debug("Got external packet-in from " + event.getChannel().getRemoteAddress().toString());

                        // TODO handling business here

                        GatewayMsg res = new GatewayMsg((byte)1, Settings.socketAddr);

                        ChannelFuture write = event.getChannel().write(res);
                        write.awaitUninterruptibly();
                        if (write.isSuccess()){
                            log.debug("External Packet-In send out");
                        }
                    } // end of if external
                } // end of if packet-in

                else if (msg.getType() == MessageType.PACKET_OUT.getType()){
                    log.info("handler packet out");
                }
            }
        };
        NUCtrl_daemon daemon = new NUCtrl_daemon(new MessageHandler(packetWorker));
        daemon.run();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GatewayMsg msg = new GatewayMsg((byte)0, Settings.socketAddr);
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
