package ONExClient.onex4j;

import ONExClient.onex4j.Daemon.ONExDaemon;
import ONExClient.onex4j.Interface.ITopology;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-18
 * Time: PM11:46
 */
public class TopologyDealer implements ITopology {
    LocalTopo localTopo;

    SwitchDealer switchDealer;
    ONExDaemon onExDaemon;

    public TopologyDealer(SwitchDealer sw_h) {
        this.switchDealer = sw_h;
    }

    public void setDaemon(ONExDaemon daemon){
        this.onExDaemon = daemon;
    }

    @Override
    public void getGlobalTopo() {
        //To change body of implemented methods use File | ONExSetting | File Templates.
    }

    @Override
    public void getLocalTopo() {
        switchDealer.getTopo();
    }

    @Override
    public void updateLocalTopo() {
        onExDaemon.sendONEx(null);
    }

    @Override
    public void parseGlobalTopo() {
        //To change body of implemented methods use File | ONExSetting | File Templates.
    }
}
