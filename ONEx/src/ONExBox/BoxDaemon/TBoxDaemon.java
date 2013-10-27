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

        GlobalTopo globalTopo = new GlobalTopo();
        globalTopo.addHostEntry(123L, (short)123, 123, new byte[]{1,2,3,4,5,6});
        globalTopo.addSwitchLink(1L, (short)1, 2L, (short)2);

        ONExPacket op = ONExProtocolFactory.ONExUploadLocalTopo(globalTopo);

        assert bos != null;
        try {
            bos.write(op.toByteBuffer().array());
            bos.flush();
            Thread.sleep(1000);
            bos.write(op.toByteBuffer().array());
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("test for NOT DUPLICATED: " + gateway.getGlobalTopo().toString());

        globalTopo.addHostEntry(124L, (short)123, 123, new byte[]{1,2,3,4,5,6});
        globalTopo.addSwitchLink(5L, (short)5, 2L, (short)2);

        try {
            bos.write(ONExProtocolFactory.ONExUploadLocalTopo(globalTopo).toByteBuffer().array());
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("test for MERGING: " + gateway.getGlobalTopo().toString());

        /* test for  */
        op = ONExProtocolFactory.ONExRequestGlobalTopo();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bos.write(op.toByteBuffer().array());
            bos.flush();
            assert bis!=null;
            int idx = 0;
            ByteBuffer buf = ByteBuffer.allocate(4);
            int length = 0;
            while(true){
                int temp = bis.read();

                /* manually got length */
                if (idx <= 4 && idx >= 1){
                    buf.put((byte)temp);
                    if (idx == 4){
                        buf.flip();
                        length = buf.getInt();
                    }
                }
                idx += 1;
                baos.write(temp);
                if (baos.size() == length)
                    break;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ONExPacket newOp = ONExProtocolFactory.parser(baos.toByteArray());
        log.info(newOp.toString());
        log.info(newOp.getGlobalTopo().toString());
    }
}
