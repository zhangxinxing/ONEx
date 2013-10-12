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
public class NUCtrl_daemon implements API {
    // exposed API

    private Core core;
    private Gateway gateway;

    public NUCtrl_daemon() {
        gateway = new Gateway();
        core = new Core(gateway);
        Settings.getInstance().init();
    }

    // setup
    public void run(){
        gateway.setup();
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
        NUCtrl_daemon daemon = new NUCtrl_daemon();
        daemon.run();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GatewayMsg msg = new GatewayMsg((byte)0, Settings.myAddr);
        int TEST = 1;
        for (int i = 0; i < TEST; i++){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                break;
            }
            daemon.onPacketIn(msg);
        }
    }
}
