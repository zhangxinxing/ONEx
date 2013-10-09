package nuctrl.core;

import nuctrl.interfaces.PacketOutWorker;
import nuctrl.protocol.GatewayMsg;

import java.util.LinkedList;
import java.util.List;

/**
 * User: fan
 * Date: 13-10-9
 * Time: AM10:52
 */
public class OutHandler implements Handler{
    private List<GatewayMsg> PacketOutBuffer;
    private PacketOutWorker packetOutWorker;

    public OutHandler() {
        PacketOutBuffer = new LinkedList<GatewayMsg>();
        packetOutWorker = new PacketOutWorker() {
            @Override
            public void handlePacketOut(GatewayMsg msg) {
                // hello here
            }
        };

        // a thread warping the worker
        Thread handler = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        PacketOutBuffer.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }

                    // after wake up
                    packetOutWorker.handlePacketOut(PacketOutBuffer.get(0));
                    PacketOutBuffer.remove(0);
                }
            }
        });

        handler.run();
    }

    @Override
    public void insert(GatewayMsg msg) {
        if (msg == null){
            System.out.println("[OutHandler] null");
        }
        PacketOutBuffer.add(msg);
    }

}
