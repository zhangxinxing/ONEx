package nuctrl.test;

import nuctrl.NUCtrlDaemon;
import nuctrl.Settings;
import nuctrl.core.MessageHandler;
import nuctrl.interfaces.PacketHandler;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.MessageType;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.MessageEvent;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-13
 * Time: AM10:58
 */
public class TDaemon {
    public static void main(String[] args){
        PacketHandler packetHandler = new demoHandler();
        NUCtrlDaemon daemon = new NUCtrlDaemon(new MessageHandler(packetHandler));

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

class demoHandler implements PacketHandler{
    private static Logger log = Logger.getLogger(demoHandler.class);

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
}