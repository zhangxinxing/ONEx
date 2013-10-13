package nuctrl.test;

import nuctrl.NUCtrlDaemon;
import nuctrl.Settings;
import nuctrl.core.Benchmark;
import nuctrl.core.MessageHandler;
import nuctrl.interfaces.PacketHandler;
import nuctrl.protocol.GatewayMsg;
import nuctrl.protocol.MessageType;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.MessageEvent;
import org.apache.log4j.Logger;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-13
 * Time: AM10:58
 */
public class TDaemon {
    private static Logger log = Logger.getLogger(TDaemon.class);

    public static void main(String[] args){
        PacketHandler packetHandler = new demoHandler();
        NUCtrlDaemon daemon = new NUCtrlDaemon(new MessageHandler(packetHandler));

        log.info(">>>BEGIN Test");
        Benchmark.startBenchmark();

        if (args.length == 0){
            GatewayMsg msg = new GatewayMsg((byte)0, Settings.getInstance().socketAddr);

            for (int i = 0; i < Settings.TEST_RUN; i++){
                try {
                    Thread.sleep(Settings.PACKET_INTERVAL);
                } catch (InterruptedException e) {
                    break;
                }
                daemon.daemonOnPacket(msg);
            }

        }
        System.out.print(Benchmark.endBenchmark());
        log.info(">>>END Test");
    }
}

class demoHandler implements PacketHandler{
    private static Logger log = Logger.getLogger(demoHandler.class);

    @Override
    public void onPacket(GatewayMsg msg) {
        if (msg.getType() == MessageType.PACKET_IN.getType()){
            if (msg.getEvent() != null){
                log.debug("Handle remote Packet-In");
                Benchmark.addRemotePktIn();

                MessageEvent event = msg.getEvent();
                log.debug("From " + event.getChannel().getRemoteAddress().toString());

                randomBusy(Settings.MAX_WHILE_LOOP); // random busy
                GatewayMsg res = new GatewayMsg((byte)1, Settings.getInstance().socketAddr);
                ChannelFuture write = event.getChannel().write(res);
                write.awaitUninterruptibly();
                if (write.isSuccess()){
                    log.debug("Packet-Out send out");
                }
            }
            else {
                log.debug("Local Packet-In");
                Benchmark.addLocalPktIn();

                randomBusy(Settings.MAX_WHILE_LOOP); // random busy
            }
        } // end of if packet-in

        else if (msg.getType() == MessageType.PACKET_OUT.getType()){
            log.debug("handler packet out");
            Benchmark.addRemotePktOut();
            randomBusy(1000);
        }
    }

    private void randomBusy(int max){
        int i = (new Random().nextInt() % max);
        i = (i>0)?i:-i;
        while(i > 0){
            i--;
        }
    }
}