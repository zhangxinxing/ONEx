package nuctrl.core;

import nuctrl.interfaces.PacketHandler;
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
public class MessageHandler implements nuctrl.interfaces.MessageHandler, Runnable {

    private static Logger log = Logger.getLogger(MessageHandler.class);
    private static final List<GatewayMsg> PacketQueue = new LinkedList<GatewayMsg>();
    public PacketHandler packetHandler;

    private volatile boolean running;

    public MessageHandler(PacketHandler worker) {
        this.running = true;
        if (worker == null) {
            log.error("Null worker is not allowed");
            System.exit(-1);
        }
        else{
            if (packetHandler == null){
                packetHandler = worker;
        }
        }
    }

    @Override
    public void insert(GatewayMsg msg) {
        if (msg == null){
            log.error("[MessageHandler] null");
            return;
        }
        synchronized (PacketQueue){
            PacketQueue.add(msg);
            PacketQueue.notify();
        }
        log.debug("insert " + msg.toString());
    }

    public void terminate(){
        running = false;
    }

    @Override
    public void run() {
        while(running){
            synchronized (PacketQueue){
                try {
                    while(PacketQueue.size() == 0){
                        PacketQueue.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                log.debug("worker wake up");
                for (GatewayMsg msg : PacketQueue){
                    log.error(">>>>> before remove " + PacketQueue.size());
                    PacketQueue.remove(msg);
                    log.error(">>>>> after remove " + PacketQueue.size());
                    packetHandler.onPacket(msg);
                }
                log.debug("===== size: " + PacketQueue.size());
            }
        }
    }

    public static int sizeOfQueue(){
        synchronized (PacketQueue){
            return PacketQueue.size();
        }
    }
}
