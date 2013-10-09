package nuctrl;

import nuctrl.core.Core;
import nuctrl.gateway.Gateway;
import nuctrl.interfaces.API;
import nuctrl.protocol.GatewayMsg;

/**
 * User: fan
 * Date: 13-10-9
 * Time: PM3:51
 */
public class NUCtrldaemon implements API {
    // exposed API

    private Core core;
    private Gateway gateway;
    private Settings settings;

    public NUCtrldaemon() {
        core = new Core(gateway);
        gateway = new Gateway();
        settings = new Settings();
    }

    // init
    public void init(){
        core.init();
        gateway.run();
        initUpdateGlobalInfo();
    }

    // updateGlobalInfo
    private void initUpdateGlobalInfo(){
        // a thread for updating busy table regularly
        Thread updateBusyTable = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(Settings.BUSY_UPDATE_INT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    // every 1s
                    gateway.getDataSharing().updateBusyTable();
                }
            }
        });

        updateBusyTable.start();
    }

    public void onPacketIn(GatewayMsg msg){
       core.dispatchFunc(msg);
    }

    public static void main(String[] args){
        NUCtrldaemon daemon = new NUCtrldaemon();
        daemon.init();
    }
}
