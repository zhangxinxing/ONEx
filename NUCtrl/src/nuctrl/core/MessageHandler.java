package nuctrl.core;

import nuctrl.interfaces.Handler;
import nuctrl.interfaces.PacketWorker;
import nuctrl.protocol.GatewayMsg;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-10
 * Time: AM11:53
 */
public class MessageHandler implements Handler, Runnable {

    private static Logger log = Logger.getLogger(MessageHandler.class);
    // static list and worker to ensure only one handler per machine
    private static final List<GatewayMsg> PacketBuffer = new LinkedList<GatewayMsg>();
    private static PacketWorker packetWorker;

    public MessageHandler(PacketWorker worker) {
        if (worker == null) {
            log.error("Null worker is not allowed");
            System.exit(-1);
        }
        else{
            if (packetWorker == null){
                packetWorker = worker;
        }
        }
    }

    @Override
    public void insert(GatewayMsg msg) {
        if (msg == null){
            log.error("[Handler] null");
            return;
        }
        synchronized (PacketBuffer){
            PacketBuffer.add(msg);
            PacketBuffer.notify();
        }
        log.debug("insert " + msg.toString());
    }

    // thread of worker
    @Override
    public void run() {
        while(true){
            synchronized (PacketBuffer){
                try {
                    PacketBuffer.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

            // after wake up

            log.debug("worker wake up");
            packetWorker.onpPacket(PacketBuffer.get(0));
            PacketBuffer.remove(0);
        }
    }
}
