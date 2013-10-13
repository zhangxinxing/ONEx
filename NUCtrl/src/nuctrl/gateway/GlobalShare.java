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
public class GlobalShare {
    /* */
    private HazelcastInstance hz;
    private static Logger log;
    private Monitor mn;

    // local Data
    private BusyTableEntry localBt;
    private List<TopologyTableEntry> localTopo;

    public GlobalShare(){
        log = Logger.getLogger(GlobalShare.class.getName());
        mn = Monitor.getInstance();

        /* HazelCast */
        Config cfg = new Config();
        this.hz = Hazelcast.newHazelcastInstance(cfg);

        // initialization
        this.localBt = new BusyTableEntry(Settings.getInstance().socketAddr);
        this.localTopo = new LinkedList<TopologyTableEntry>();
        this.initUpdatingThread();
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

        log.debug(">>>begin [getWhoIsIdle]");

        Set<InetSocketAddress> keySet = map.keySet();

        InetSocketAddress maybeKey = null;

        for (InetSocketAddress key : keySet){

            if (maybeKey == null)
                maybeKey = key;

            BusyTableEntry bt = map.get(key);

            // TODO add more complex logic
            log.debug("SizeOfQueue " + key.toString() + ": " + bt.getSizeOfQueueIn());

            if(bt.getSizeOfQueueIn() < 5){
                idle.add(key);
            }

            if(bt.getSizeOfQueueIn() < map.get(maybeKey).getSizeOfQueueIn())
                maybeKey = key;
        }

        if (idle.size() == 0){
            idle.add(maybeKey);
        }
        log.debug(">>>end #idles " + idle.size());
        return idle;

    }

    public boolean updateBusyTable(){
        localBt.setSizeOfQueueIn(mn.getSizeOfQueueIn());
        localBt.setCpuAccount(mn.getCPUAccount());
        updateRemoteBusyTable();
        return true;
    }

    private boolean updateRemoteBusyTable(){
        ConcurrentMap<InetSocketAddress, BusyTableEntry> map = hz.getMap(Settings.BUSYTABLE_MAP);
        InetSocketAddress localAddr = Settings.getInstance().socketAddr;

        if(localAddr == null){
            log.error("Error in config: socketAddr == null");
            System.exit(-1);
        }

        if(map.get(localAddr) == null){
            log.info("Put new entry into busyTable for " + localAddr);
            map.put(localAddr, localBt);
        }
        else{
            log.debug("update busyTable for " + localAddr);
            map.replace(localAddr, localBt);
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

    // updateGlobalInfo
    private void initUpdatingThread(){
        // a thread for updating busy table regularly
        updateBusyTable();
        Thread updatingDaemon = new Thread(new Runnable() {
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
                    updateBusyTable();
                }
            }
        });

        updatingDaemon.start();
    }
}
