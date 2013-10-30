package ONExProtocol;

/*
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-19
 * Time: AM10:27
 */

import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.File;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/*
    A thread-safe class inherit from local topology
  */
public class GlobalTopo  extends LocalTopo{

    DirectedGraph<Long, DefaultEdge> forestNodeGraph;

    public GlobalTopo() {
        super();
        forestNodeGraph = new SimpleDirectedGraph<Long, DefaultEdge>(DefaultEdge.class);
    }

    public synchronized void loadFromDB(String db){
        if(!new File(db).exists()){
            log.error("DB file "  + db + " does not exist");
            return;
        }
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

            ResultSet rs = statement.executeQuery(String.format("SELECT * FROM %s", SQLiteHelper.T_HOSTS));

            while(rs.next()){
                this.addHost(
                        rs.getLong("dpid"),
                        rs.getShort("port"),
                        rs.getInt("ipv4"),
                        Util.longToMAC(rs.getLong("MAC")));
            }

            rs = statement.executeQuery(String.format("SELECT * FROM %s", SQLiteHelper.T_SWLINKS));
            while(rs.next()){
                this.addSwitchLink(
                        rs.getLong("src"),
                        rs.getShort("srcPort"),
                        rs.getLong("dst"),
                        rs.getShort("dstPort"));

            }

            rs = statement.executeQuery(String.format("SELECT * FROM %s", SQLiteHelper.T_FOREST));
            while(rs.next()){
                this.addForestEntry(
                        rs.getInt("controllerIP"),
                        rs.getShort("controllerPort"),
                        rs.getLong("dpid"));
            }
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

    public ForestEntry getForestByDpid(long dpid){
        synchronized (forestEntrySet){
            for(ForestEntry entry : forestEntrySet){
                if(entry.getDpid() == dpid){
                    return new ForestEntry(entry);
                }
            }
        }
        return null;
    }

    public List<SwitchLink> findGlobalWayToDpid(long src, long dst){


        ForestEntry srcForest = getForestByDpid(src);
        ForestEntry dstForest = getForestByDpid(dst);

        if (srcForest.getForestNode() == dstForest.getForestNode()){
            log.error(src + " and " + dst
                    + " are in the same Forest, should be detected early");
            return null;
        }

        for (SwitchLink link : switchLinkSet){
            long v1 = getForestByDpid(link.getSrcDpid()).getForestNode();
            long v2 = getForestByDpid(link.getDstDpid()).getForestNode();

            if (forestNodeGraph.getEdge(v1, v2) == null){
                forestNodeGraph.addVertex(v1);
                forestNodeGraph.addVertex(v2);
                forestNodeGraph.addEdge(v1, v2);
                forestNodeGraph.getEdge(v1, v2);
            }

        }
        DijkstraShortestPath sp =
                new DijkstraShortestPath(forestNodeGraph, srcForest.getForestNode(), dstForest.getForestNode());

        GraphPath path = sp.getPath();
        DefaultEdge e = (DefaultEdge)path.getEdgeList().get(0);

        long nextForestNode = forestNodeGraph.getEdgeTarget(e);


        /*
            TODO, can be done by SQL
         */
        List<SwitchLink> links = new LinkedList<SwitchLink>();
        for (SwitchLink link : switchLinkSet){
            long v1 = getForestByDpid(link.getSrcDpid()).getForestNode();
            long v2 = getForestByDpid(link.getDstDpid()).getForestNode();

            if (v1 == srcForest.getForestNode() && v2 == nextForestNode)
                links.add(link);

        }

        return links;
    }

    public String toString(){
        String to = String.format(
                "[GlobalTopo, #host=%d, #switch=%d, #forest=%d]",
                hostEntryMap.size(),
                switchLinkSet.size(),
                forestEntrySet.size()
        );
        for (HostEntry hostEntry : hostEntryMap.values()){
            to += hostEntry.toString();
        }
        for (SwitchLink swLink : switchLinkSet){
            to += swLink.toString();
        }

        for (ForestEntry entry : forestEntrySet){
            to += entry.toString();
        }

        return to;
    }
}
