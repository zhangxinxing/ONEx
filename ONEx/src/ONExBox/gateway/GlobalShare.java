package ONExBox.gateway;

import ONExBox.Monitor;
import ONExBox.ONExSetting;
import ONExBox.protocol.BusyTableEntry;
import ONExProtocol.*;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * User: fan
 * Date: 13-10-3
 * Time: PM8:19
 */
public class GlobalShare{
    /* */
    private HazelcastInstance hz;
    private static Logger log;
    private Monitor mn;
    private volatile boolean runningDaemon = true;
    private ExecutorService exec;

    // local Data
    private BusyTableEntry localBt;

    //
    private static final String SWITCH_LINKS = "sw";
    private static final String HOST_ENTRIES = "hosts";
    private static final String FORESTS = "forest";

    public GlobalShare(){
        log = Logger.getLogger(GlobalShare.class.getName());
        mn = Monitor.getInstance();

        exec = Executors.newSingleThreadExecutor();

        /* HazelCast */
        Config cfg = new Config();
        this.hz = Hazelcast.newHazelcastInstance(cfg);

        // initialization
        this.localBt = new BusyTableEntry(ONExSetting.getInstance().socketAddr);

        // important
        updateBusyTable();

        exec.execute(new Runnable() {
            @Override
            public void run() {
                while(runningDaemon){
                    try {
                        Thread.sleep(ONExSetting.BUSY_UPDATE_INT);
                        if (!runningDaemon){
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    // every 1s
                    updateBusyTable();
                }
            }
        });
    }

    /* Busy Table */
    private boolean getBusyTableByAddr(InetSocketAddress addr){
        log.info("getting busyTable of " + addr.toString());
        ConcurrentMap<InetSocketAddress, BusyTableEntry> map = hz.getMap(ONExSetting.BUSYTABLE_MAP);
        BusyTableEntry bt = map.get(addr);
        if (bt != null){
            log.debug(bt.toString());
        }
        return (bt != null);
    }

    public Map getBusyTableOnline(){
        return hz.getMap(ONExSetting.BUSYTABLE_MAP);
    }

    public List<InetSocketAddress> getWhoIsIdle(){
        ConcurrentMap<InetSocketAddress, BusyTableEntry> map = hz.getMap(ONExSetting.BUSYTABLE_MAP);
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
        localBt.setCpuAccount(mn.getCPUAccount());
        updateRemoteBusyTable();
        return true;
    }

    private boolean updateRemoteBusyTable(){
        ConcurrentMap<InetSocketAddress, BusyTableEntry> map = hz.getMap(ONExSetting.BUSYTABLE_MAP);
        InetSocketAddress localAddr = ONExSetting.getInstance().socketAddr;

        if(localAddr == null){
            log.error("Error in config: socketAddr == null");
            System.exit(-1);
        }

        if(map.get(localAddr) == null){
            log.info("Put new entry into busyTable for " + localAddr);
            map.put(localAddr, localBt);
        }
        else{
            log.trace("update busyTable for " + localAddr);
            map.replace(localAddr, localBt);
        }

        return true;
    }

    /* TOPOLOGY */
    public boolean mergeTopology(GlobalTopo topo){
        // get remote objects
        Set<HostEntry> hostEntries = hz.getSet(HOST_ENTRIES);
        Set<SwitchLink> switchLinks = hz.getSet(SWITCH_LINKS);
        Set<ForestEntry> forestEntrySet = hz.getSet(FORESTS);

        // add new entries and links
        if (hostEntries.addAll(topo.getHostEntrySet()) ||
                switchLinks.addAll(topo.getSwitchLinkSet()) ||
                forestEntrySet.addAll(topo.getForestEntrySet())){

            log.debug("Merged topology with remote version");
            GlobalTopo globalTopo = new GlobalTopo();
            globalTopo.addHostsAll(hostEntries);
            globalTopo.addSwitchLinksAll(switchLinks);
            globalTopo.addForestEntriesAll(forestEntrySet);
            globalTopo.writeToDB("jdbc:sqlite:" + SQLiteHelper.SQLITE_DB_GLOBALTOPO);
            return true;
        }

        return false;
    }


    public void shutdown(){
        // step 1: shutdown daemon
        this.runningDaemon = false;
        try {
            exec.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // step2 close hazelcast
        Hazelcast.shutdownAll();
    }
}