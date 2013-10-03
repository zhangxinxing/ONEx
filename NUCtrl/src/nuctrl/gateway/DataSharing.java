package nuctrl.gateway;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import nuctrl.Settings;
import nuctrl.core.impl.Monitor;
import nuctrl.interfaces.BusyChangeCallback;
import nuctrl.interfaces.TopologyChangeCallback;
import nuctrl.protocol.BusyTableEntry;

import java.util.concurrent.ConcurrentMap;

/**
 * User: fan
 * Date: 13-10-3
 * Time: PM8:19
 */
public class DataSharing implements Runnable, BusyChangeCallback, TopologyChangeCallback{
    private HazelcastInstance hz;
    private int myID;
    private BusyTableEntry localBt;

    public DataSharing(){
        myID = Settings.getLocalID();
        Config cfg = new Config();
        this.hz = Hazelcast.newHazelcastInstance(cfg);
        this.localBt = new BusyTableEntry(myID);
    }

    private boolean getBusyTable(int id){
        ConcurrentMap<Integer, BusyTableEntry> map = hz.getMap(Settings.BUSYTABLE_MAP);
        BusyTableEntry bt = map.get(id);
        if (bt != null){
            System.out.println(bt.toString());
        }
        return (bt != null);
    }

    @Override
    public boolean updateLocalBusyTable(){
        ConcurrentMap<Integer, BusyTableEntry> map = hz.getMap(Settings.BUSYTABLE_MAP);
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
        try {
            Thread.sleep(1000);
            this.updateLocalBusyTable();

            while(true){
                Thread.sleep(500);
                this.getBusyTable(myID);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean updateTopology() {
        // TODO
        return false;
    }
}
