package nuctrl.core;

import nuctrl.interfaces.PacketOutWorker;
import nuctrl.protocol.GatewayMsg;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * User: fan
 * Date: 13-10-9
 * Time: AM10:52
 */
public class OutHandler implements Handler, Runnable{
    private static Logger log =
            Logger.getLogger(OutHandler.class);
    private static final List<GatewayMsg> PacketOutBuffer
            = new LinkedList<GatewayMsg>();
    private static PacketOutWorker packetOutWorker;

    public OutHandler() {
        packetOutWorker = new PacketOutWorker() {
            @Override
            public void handlePacketOut(GatewayMsg msg) {
                log.debug("[Out-worker] " + msg.toString());
                // hello here
            }
        };

    }

    @Override
    public void insert(GatewayMsg msg) {
        if (msg == null){
            log.error("[OutHandler] null");
        }
        synchronized (PacketOutBuffer){
            PacketOutBuffer.add(msg);
            PacketOutBuffer.notify();
        }
    }

    // thread of worker
    @Override
    public void run() {
        // TODO add packet out handler here
        while(true){
            synchronized (PacketOutBuffer){
                try {
                    PacketOutBuffer.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

            // after wake up
            log.debug("[Out-worker] wake up");
            packetOutWorker.handlePacketOut(PacketOutBuffer.get(0));
            PacketOutBuffer.remove(0);
        }
    }
}
