package nuctrl.interfaces;

import nuctrl.protocol.GatewayMsg;

/**
 * User: fan
 * Date: 13-10-9
 * Time: AM11:14
 */
public interface MessageHandler {
    public void insert(GatewayMsg msg);
}
