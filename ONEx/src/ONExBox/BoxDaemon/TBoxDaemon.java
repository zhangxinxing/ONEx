package ONExBox.BoxDaemon;

import ONExBox.gateway.Gateway;
import ONExProtocol.GlobalTopo;
import ONExProtocol.ONExPacket;
import ONExProtocol.ONExProtocolFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-20
 * Time: AM11:50
 */
public class TBoxDaemon {
    private static Logger log = Logger.getLogger(TBoxDaemon.class);

    public static void main(String[] args){
        Gateway gateway = new Gateway();

        // mock 2 clients
        testUploadTopology(gateway);



    }

    private static void testUploadTopology(Gateway gateway){
        Socket socket1;
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            socket1 = new Socket("localhost", 12345);
            log.info(socket1.toString());
            bos = new BufferedOutputStream(socket1.getOutputStream());
            bis = new BufferedInputStream(socket1.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
