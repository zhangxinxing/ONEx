package nuctrl.test;

import nuctrl.gateway.GlobalShare;

/**
 * User: fan
 * Date: 13-10-3
 * Time: PM9:21
 */
public class TDataSharing{

    public static void main(String[] args){
        GlobalShare ds = new GlobalShare();
        ds.updateBusyTable();
        ds.getWhoIsIdle();

        ds.updateBusyTable(); // update again

        ds.updateTopology();
    }
}
