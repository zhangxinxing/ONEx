package nuctrl.interfaces;

import nuctrl.protocol.GatewayMsg;

/**
 * User: fan
 * Date: 13-10-9
 * Time: PM5:01
 */
public interface PacketWorker {
    public void onpPacket(GatewayMsg msg);
}