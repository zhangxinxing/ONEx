package nuctrl.test;

import nuctrl.Settings;
import nuctrl.gateway.DataSharing;

/**
 * User: fan
 * Date: 13-10-3
 * Time: PM9:21
 */
public class TDataSharing{

    public static void main(String[] args){
        Settings set = new Settings();
        DataSharing ds = new DataSharing();
        ds.updateBusyTable();
        ds.getWhoIsIdle();

        ds.updateBusyTable(); // update again

        ds.updateTopology();
    }
}
