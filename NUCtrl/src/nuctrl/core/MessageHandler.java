package nuctrl.core;

import nuctrl.Settings;
import nuctrl.interfaces.PacketHandler;
import nuctrl.protocol.GatewayMsg;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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

    private ExecutorService exec;

    private volatile boolean running;

    public MessageHandler(PacketHandler worker) {
        this.running = true;
        exec = Executors.newFixedThreadPool(Settings.SIZE_OF_POOL);
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

    public void onMessageForThreadPool(GatewayMsg msg){
        final GatewayMsg inMsg = msg;
        exec.execute(new Runnable() {
            @Override
            public void run() {
                packetHandler.onPacket(inMsg);
            }
        });
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
                Iterator<GatewayMsg> it = PacketQueue.iterator();
                while(it.hasNext()){
                    GatewayMsg msg = it.next();
                    it.remove();
                    packetHandler.onPacket(msg);
                }
            }
        }
    }

    public static int sizeOfQueue(){
        synchronized (PacketQueue){
            return PacketQueue.size();
        }
    }
}
