package nuctrl.core;

import nuctrl.interfaces.PacketInWorker;
import nuctrl.protocol.GatewayMsg;

import java.util.LinkedList;
import java.util.List;

/**
 * User: fan
 * Date: 13-10-9
 * Time: AM10:52
 */
public class InHandler implements Handler {

    private List<GatewayMsg> PacketInBuffer;
    private PacketInWorker packetInWorker;

    public InHandler() {
        PacketInBuffer = new LinkedList<GatewayMsg>();
        packetInWorker = new PacketInWorker() {
            @Override
            public void handlePacketIn(GatewayMsg msg) {
                // hello here
            }
        };

        // a thread warping the worker
        Thread handler = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        PacketInBuffer.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }

                    // after wake up
                    packetInWorker.handlePacketIn(PacketInBuffer.get(0));
                    PacketInBuffer.remove(0);
                }
            }
        });

        handler.run();
    }

    @Override
    public void insert(GatewayMsg msg) {
        if (msg == null){
            System.out.println("[InHandler] null");
        }
        PacketInBuffer.add(msg);
    }
}
