package nuctrl.gateway;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;
import nuctrl.Settings;
import nuctrl.core.Monitor;
import nuctrl.protocol.BusyTableEntry;
import nuctrl.protocol.TopologyTableEntry;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * User: fan
 * Date: 13-10-3
 * Time: PM8:19
 */
public class DataSharing {
    /* */
    private HazelcastInstance hz;
    private static Logger log;
    private Monitor mn;

    // local Data
    private BusyTableEntry localBt;
    private List<TopologyTableEntry> localTopo;

    public DataSharing(){
        /* static field */
        log = Logger.getLogger(DataSharing.class.getName());
        mn = Monitor.getInstance();

        /* hazelcast */
        Config cfg = new Config();
        this.hz = Hazelcast.newHazelcastInstance(cfg);

        // initialization
        this.localBt = new BusyTableEntry(Settings.myAddr);
        this.localTopo = new LinkedList<TopologyTableEntry>();

    }

    /* Busy Table */
    private boolean getBusyTableByAddr(InetSocketAddress addr){
        log.info("getting busyTable of " + addr.toString());
        ConcurrentMap<InetSocketAddress, BusyTableEntry> map = hz.getMap(Settings.BUSYTABLE_MAP);
        BusyTableEntry bt = map.get(addr);
        if (bt != null){
            log.debug(bt.toString());
        }
        return (bt != null);
    }

    public Map getBusyTableOnline(){
        return hz.getMap(Settings.BUSYTABLE_MAP);
    }

    public List<InetSocketAddress> getWhoIsIdle(){
        ConcurrentMap<InetSocketAddress, BusyTableEntry> map = hz.getMap(Settings.BUSYTABLE_MAP);
        List<InetSocketAddress> idle = new LinkedList<InetSocketAddress>();

        log.debug("[getWhoIsIdle]");

        Set<InetSocketAddress> keySet = map.keySet();

        InetSocketAddress maybeKey = null;

        for (InetSocketAddress key : keySet){

            if (maybeKey == null)
                maybeKey = key;

            BusyTableEntry bt = map.get(key);

            // TODO add more complex logic
            log.debug("SizeOfQueue" + key.toString() + ":" + bt.getSizeOfQueueIn());

            if(bt.getSizeOfQueueIn() < 5){
                idle.add(key);
            }

            if(bt.getSizeOfQueueIn() < map.get(maybeKey).getSizeOfQueueIn())
                maybeKey = key;
        }

        if (idle.size() == 0){
            idle.add(maybeKey);
            log.debug("#idles " + idle.size());
        }
        return idle;
    }

    public boolean updateBusyTable(){
        localBt.setSizeOfQueueIn(mn.getSizeOfQueueIn());
        localBt.setCpuAccount(mn.getCpuAccount());
        updateRemoteBusyTable();
        return true;
    }

    private boolean updateRemoteBusyTable(){
        ConcurrentMap<InetSocketAddress, BusyTableEntry> map = hz.getMap(Settings.BUSYTABLE_MAP);

        if(map.get(Settings.myAddr) == null){
            log.debug("Put new entry into busyTable for " + Settings.myAddr.toString());
            map.put(Settings.myAddr, localBt);
        }
        else{
            log.debug("update busyTable for " + Settings.myAddr.toString());
            map.replace(Settings.myAddr, localBt);
        }

        return true;
    }


    /* TOPOLOGY */
    public boolean updateTopology(){
        // TODO do sth
        updateRemoteTopology();
        return true;
    }

    private boolean updateRemoteTopology() {
        MultiMap<InetAddress, TopologyTableEntry> map = hz.getMultiMap(Settings.TOPO_MAP);

        log.debug("update remote topology #" + this.localTopo.size());
        for (TopologyTableEntry entry : this.localTopo){
            map.put(entry.getSrc(), entry);
        }
        return true;
    }
}
