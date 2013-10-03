package nuctrl.test;

import nuctrl.gateway.DataSharing;

/**
 * User: fan
 * Date: 13-10-3
 * Time: PM9:21
 */
public class TDataSharing{

    public static void main(String[] args){
        DataSharing ds = new DataSharing();


        Thread t = new Thread(ds);
        t.start();
    }

}
