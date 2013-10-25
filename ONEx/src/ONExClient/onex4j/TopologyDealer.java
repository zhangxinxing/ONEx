package ONExClient.onex4j;

import ONExClient.onex4j.SDKDaemon.SDKDaemon;
import ONExProtocol.GlobalTopo;
import ONExProtocol.LocalTopo;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-18
 * Time: PM11:46
 */
public abstract class TopologyDealer {
    LocalTopo localTopo;
    SDKDaemon SDKDaemon;

    public TopologyDealer() {
        localTopo = new LocalTopo();
    }

    public void setDaemon(SDKDaemon daemon){
        this.SDKDaemon = daemon;
    }

    public abstract void getGlobalTopo();

    public abstract LocalTopo getLocalTopo();

    public abstract void updateLocalTopo();

    public abstract void parseGlobalTopo(GlobalTopo topo);
}
