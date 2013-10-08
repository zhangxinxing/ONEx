package nuctrl.test;

import nuctrl.gateway.IOClient;

import java.util.Date;

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

        IOClient ioClient = new IOClient(host, port);

        ioClient.init();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ioClient.send(new Date());

    }
}
