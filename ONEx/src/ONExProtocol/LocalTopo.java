package ONExProtocol;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM9:47
 */


/*
    LocalTopo: a *thread-safe* class handling local/partial topology
 */
public class LocalTopo {
    protected final Map<Long, HostEntry> hostEntryMap;
    protected final Set<ForestEntry> forestEntrySet;  /* assume that one sw connects to one controller*/
    protected final Set<SwitchLink> switchLinkSet;

    protected static Logger log = Logger.getLogger(LocalTopo.class);

    public LocalTopo() {
        hostEntryMap = new HashMap<Long, HostEntry>();
        forestEntrySet = new HashSet<ForestEntry>();
        switchLinkSet = new HashSet<SwitchLink>();
    }

    /*
        Assume that one device has ONLY one attach point
     */
    public boolean addHost(long dpid, short port, int ipv4, byte[] MAC){
        if (MAC == null || MAC.length != 6){
            log.error("error MAC address");
            return false;
        }

        synchronized (hostEntryMap){
            HostEntry host = hostEntryMap.get(Util.MACToLong(MAC));

            if (host == null){
                hostEntryMap.put(Util.MACToLong(MAC), new HostEntry(dpid, port, ipv4, MAC));
                log.debug("host added: " + Util.macToString(MAC));
                return true;
            }
            else{
                log.debug(String.format("addHost ignored: %s", Util.macToString(MAC)));
                return false;
            }
        }
    }

    public boolean addHostsAll(Set<HostEntry> hostEntrySet){
        return hostEntrySet.addAll(hostEntrySet);
    }

    /*
        Part of IDeviceListener
     */
    public void updateHostIPv4(long MAC, int ipv4){
        synchronized (hostEntryMap){
            HostEntry host = hostEntryMap.get(MAC);
            if (host == null){
                log.error("update a non-exist host");
            }
            else{
                int oldIP = host.getIpv4();
                host.setIpv4(ipv4);
                log.debug(String.format("device %s Ip changed from %s to %s",
                        Util.macToString(Util.longToMAC(MAC)),
                        Util.ipToString(oldIP),
                        Util.ipToString(ipv4)));
            }
        }
    }

    public void removeHost(long MAC){
        synchronized (hostEntryMap){
            HostEntry re = hostEntryMap.remove(MAC);
            if (re != null){
                log.debug("removed " + re.toString());
            }
            else {
                log.debug("try to remove: " + Util.macToString(Util.longToMAC(MAC)));
            }
        }
    }

    /*
        is Host Within
     */

    public HostEntry isHostWithin(byte[] MAC){
        if (MAC == null || MAC.length != 6){
            log.error("errer MAC address");
        }
        synchronized (hostEntryMap){
            return hostEntryMap.get(Util.MACToLong(MAC));
        }
    }

    public HostEntry isHostWithin(long MAC){
        synchronized (hostEntryMap){
            return hostEntryMap.get(MAC);
        }
    }

    public void addSwitchLink(long src, short srcPort, long dst, short dstPort){
        SwitchLink newLink = new SwitchLink(src, srcPort, dst, dstPort);
        SwitchLink reverseLink = new SwitchLink(dst, dstPort, src, srcPort);
        synchronized (switchLinkSet){
            if (switchLinkSet.add(newLink)){
                log.debug("switchLink added: " + newLink.toString());
            }
            if (switchLinkSet.add(reverseLink)){
                log.debug("reverse link added: " + reverseLink.toString());
            }
        }
    }

    public boolean addSwitchLinksAll(Set<SwitchLink> switchLinkSet){
        return this.switchLinkSet.addAll(switchLinkSet);
    }

    public SwitchLink findSwitchLinkTo(long src, long dst){
        synchronized (switchLinkSet){
            for(SwitchLink link : switchLinkSet){
                if(link.getSrcDpid() == src && link.getDstDpid() == dst){
                    return link;
                }
            }
        }
        return null;
    }

    /*
        add forest entry
     */

    public void addForestEntry(int ipv4, short tcpPort, long dpid){
        ForestEntry newEntry = new ForestEntry(ipv4, tcpPort, dpid);
        synchronized (forestEntrySet){
            if(forestEntrySet.add(newEntry)){
                log.debug("forest added: " + newEntry.toString());
            }
            else{
                log.debug("addForest ignored: " + newEntry.toString());
            }
        }
    }

    public boolean addForestEntriesAll(Set<ForestEntry> forestEntrySet){
        return forestEntrySet.addAll(forestEntrySet);
    }

    /*
        all three get method are (thread-safe) copy-on-read: make a new set and return it.
     */
    public Set<SwitchLink> getSwitchLinkSet(){
        synchronized (switchLinkSet){
            // deep copy
            Set<SwitchLink> sw = new HashSet<SwitchLink>();
            sw.addAll(switchLinkSet);
            return sw;
        }
    }

    public Set<HostEntry> getHostEntrySet(){
        synchronized (hostEntryMap){
            // deep copy
            Set<HostEntry> hostEntrySet = new HashSet<HostEntry>();
            hostEntrySet.addAll(hostEntryMap.values());
            return hostEntrySet;
        }
    }

    public Set<ForestEntry> getForestEntrySet(){
        synchronized (forestEntrySet){
            Set<ForestEntry> forestEntrySet = new HashSet<ForestEntry>();
            forestEntrySet.addAll(this.forestEntrySet);
            return forestEntrySet;
        }
    }

    /*
        write to db
     */
    public synchronized void writeToDB(String db){
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection connection = null;
        try
        {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:" + db);
            Statement statement = connection.createStatement();

            statement.executeUpdate(SQLiteHelper.CREATE_TABLES);

            for(HostEntry entry: hostEntryMap.values()){
                statement.addBatch(String.format("INSERT or IGNORE INTO %s VALUES(%d, %d, %d, %d);",
                        SQLiteHelper.T_HOSTS,
                        entry.getDpid(), entry.getPort(), entry.getIpv4(), Util.MACToLong(entry.getMAC())));
            }

            for(SwitchLink entry: switchLinkSet){
                statement.addBatch(String.format("INSERT or IGNORE INTO %s VALUES(%d, %d, %d, %d);",
                        SQLiteHelper.T_SWLINKS,
                        entry.getSrcDpid(), entry.getSrcPort(), entry.getDstDpid(), entry.getDstPort()));
            }

            for(ForestEntry entry: forestEntrySet){
                statement.addBatch(String.format("INSERT or IGNORE INTO %s VALUES(%d, %d, %d);",
                        SQLiteHelper.T_FOREST,
                        entry.getControllerIP(),
                        entry.getControllerONExPort(),
                        entry.getDpid()));
            }
            statement.executeBatch();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        finally{
            try{
                if(connection != null){
                    connection.close();
                }
            }
            catch(SQLException e){
                System.err.println(e);
            }
        }
    }

    public String toString(){
        String to = "LocalDevice [#host=" + hostEntryMap.size();

        for (HostEntry host : hostEntryMap.values()){
            to += host.toString();
        }

        for (SwitchLink entry : switchLinkSet){
            to += entry.toString();
        }

        for (ForestEntry entry : forestEntrySet){
            to += entry.toString();
        }

        return to += "]";
    }
}

