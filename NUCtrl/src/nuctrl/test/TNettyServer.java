package nuctrl.test;

import nuctrl.gateway.Port.IOServer;

/**
 * User: fan
 * Date: 13-10-8
 * Time: PM9:09
 */
public class TNettyServer {
    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        IOServer ioServer = new IOServer(port);

        ioServer.init();
    }
}
