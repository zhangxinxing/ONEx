package ONExClient.onex4j.Interface;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-18
 * Time: PM11:38
 * To change this template use File | ONExSetting | File Templates.
 */
public interface ITopology {
    void getGlobalTopo();

    void getLocalTopo();

    void updateLocalTopo();

    void parseGlobalTopo();
}
