package nuctrl.test;

import com.hazelcast.config.Config;
import com.hazelcast.core.EntryView;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;


/**
 * User: fan
 * Date: 13-10-1
 * Time: PM5:51
 * To change this template use File | Settings | File Templates.
 */
public class THazelCast {
    public static void main(String[] args){
        Config cfg = new Config();

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(cfg);

        Map<Integer, String> mapCustomers = hz.getMap("customers");
        mapCustomers.put(1, "Joe");
        mapCustomers.put(2, "Ali");
        mapCustomers.put(3, "Avi");
        EntryView entry = hz.getMap("customers").getEntryView(1);
        System.out.println ("size in memory : " + entry.getCost());
        System.out.println ("creationTime : " + entry.getCreationTime());
        System.out.println ("expirationTime : " + entry.getExpirationTime());
        System.out.println ("number of hits : " + entry.getHits());
        System.out.println ("lastAccessedTime: " + entry.getLastAccessTime());
        System.out.println ("lastUpdateTime : " + entry.getLastUpdateTime());
        System.out.println ("version: " + entry.getVersion());
        System.out.println ("key: " + entry.getKey());
        System.out.println ("value: " + entry.getValue());
    }
}
