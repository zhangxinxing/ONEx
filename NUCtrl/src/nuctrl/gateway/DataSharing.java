package nuctrl.gateway;

import com.hazelcast.config.Config;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import nuctrl.Settings;
import nuctrl.core.impl.Monitor;
import nuctrl.protocol.BusyTable;

import java.util.concurrent.ConcurrentMap;

/**
 * User: fan
 * Date: 13-10-3
 * Time: PM8:19
 */
public class DataSharing implements Runnable {
    private HazelcastInstance hz;
    private int myID;
    private BusyTable localBt;

    public DataSharing(){
        myID = Settings.getLocalID();
        Config cfg = new Config();
        this.hz = Hazelcast.newHazelcastInstance(cfg);
        this.localBt = new BusyTable(myID);
    }

    private boolean getBusyTable(int id){
        ConcurrentMap<Integer, BusyTable> map = hz.getMap(Settings.BUSYTABLE_MAP);
        BusyTable bt = map.get(id);
        if (bt != null){
            System.out.println(bt.toString());
        }
        return (bt != null);
    }

    public boolean updateLocalBusyTable(){
        ConcurrentMap<Integer, BusyTable> map = hz.getMap(Settings.BUSYTABLE_MAP);
        localBt.setCpuAccountPerApp("App1", Monitor.getCpuAccount());
        localBt.setSizeOfQueueIn(Monitor.getSizeOfQueueIn());

        if(map.get(myID) == null){
            map.put(myID, localBt);
        }
        else{
            map.replace(myID, localBt);
        }

        return true;
    }

    @Override
    public void run() {
        System.out.println("myId = " + this.myID);
        try {
            while(true){
                Thread.sleep(500);
                this.getBusyTable(myID);

                Thread.sleep(1000);
                this.updateLocalBusyTable();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
