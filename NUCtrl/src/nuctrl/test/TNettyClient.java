package nuctrl.test;

import nuctrl.gateway.IOClient;

/**
 * User: fan
 * Date: 13-10-8
 * Time: PM9:08
 */
public class TNettyClient {
    public static void main(String[] args) throws Exception {

        // Parse options.
        final String host = "127.0.0.1";
        final int port = 8080;
        final int firstMessageSize = 256;

        new IOClient(host, port, firstMessageSize).run();
    }
}
