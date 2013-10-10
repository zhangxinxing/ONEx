package nuctrl.core;

import nuctrl.Settings;
import nuctrl.interfaces.PacketInWorker;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.MessageFactory;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.MessageEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * User: fan
 * Date: 13-10-9
 * Time: AM10:52
 */
public class InHandler implements Handler, Runnable {

    private static Logger log = Logger.getLogger(InHandler.class);
    // static list and worker to ensure only one handler per machine
    private static final List<GatewayMsg> PacketInBuffer = new LinkedList<GatewayMsg>();
    private static PacketInWorker packetInWorker;

    public InHandler() {
        packetInWorker = new PacketInWorker() {
            @Override
            public void handlePacketIn(GatewayMsg msg) {
                // hello here
                if (msg.getEvent() != null){
                    MessageEvent event = msg.getEvent();
                    log.debug("Got external packet-in from " + event.getChannel().getRemoteAddress().toString());

                    // TODO handling business here

                    GatewayMsg res = new GatewayMsg((byte)1, Settings.myAddr);

                    ChannelFuture write = event.getChannel().write(res);
                    write.awaitUninterruptibly();
                    if (write.isSuccess()){
                        log.debug("External Packet-In send out");
                    }

                }
            }
        };
    }

    @Override
    public void insert(GatewayMsg msg) {
        if (msg == null){
            log.error("[InHandler] null");
            return;
        }
        synchronized (PacketInBuffer){
            PacketInBuffer.add(msg);
            PacketInBuffer.notify();
        }
    }

    // thread of worker
    @Override
    public void run() {
        // TODO add packet-in handler here
        while(true){
            synchronized (PacketInBuffer){
                try {
                    PacketInBuffer.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

            // after wake up
            log.debug("[In-worker] wake up");
            packetInWorker.handlePacketIn(PacketInBuffer.get(0));
            PacketInBuffer.remove(0);
        }
    }
}
