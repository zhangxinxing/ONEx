package nuctrl.core;

import nuctrl.interfaces.PacketInWorker;
import nuctrl.protocol.GatewayMsg;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
/**
 * User: fan
 * Date: 13-10-9
 * Time: AM10:52
 */
public class InHandler implements Handler, Runnable {

    private static Logger log =
            Logger.getLogger(InHandler.class);
    private static final List<GatewayMsg> PacketInBuffer
            = new LinkedList<GatewayMsg>();
    private static PacketInWorker packetInWorker;

    public InHandler() {
        packetInWorker = new PacketInWorker() {
            @Override
            public void handlePacketIn(GatewayMsg msg) {
                log.debug("[In-worker] " + msg.toString());
                // hello here
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
