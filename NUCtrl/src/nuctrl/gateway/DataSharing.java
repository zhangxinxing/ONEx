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

    // local Data
    private BusyTableEntry localBt;
    private List<TopologyTableEntry> localTopo;

    public DataSharing(){
        Config cfg = new Config();
        this.hz = Hazelcast.newHazelcastInstance(cfg);
        log = Logger.getLogger(DataSharing.class.getName());

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
        ConcurrentMap<InetSocketAddress, BusyTableEntry> map = hz.getMap(Settings.BUSYTABLE_MAP);
        return map;
    }

    public List<InetSocketAddress> getWhoIsIdle(){
        ConcurrentMap<InetSocketAddress, BusyTableEntry> map = hz.getMap(Settings.BUSYTABLE_MAP);
        List<InetSocketAddress> idle = new LinkedList<InetSocketAddress>();

        Set<InetSocketAddress> keyset = map.keySet();
        for (InetSocketAddress key : keyset){
            BusyTableEntry bt = map.get(key);
            // TODO add more complex logic
            if(bt.getSizeOfQueueIn() < 5){
                idle.add(key);
            }
        }
        log.info("#idles " + idle.size());
        return idle;
    }

    public boolean updateBusyTable(){
        localBt.setCpuAccountPerApp("App1", Monitor.getCpuAccountByApp("App1"));
        localBt.setSizeOfQueueIn(Monitor.getSizeOfQueueIn());

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
