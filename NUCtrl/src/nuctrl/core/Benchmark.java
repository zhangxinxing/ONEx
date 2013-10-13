package nuctrl.core;

import nuctrl.Settings;
import org.hyperic.sigar.ProcTime;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-13
 * Time: PM12:52
 */
public class Benchmark {

    private static Account account;
    private static Sigar sigar = new Sigar();

    public static void startBenchmark(){
        if (Settings.getInstance().targetPid == -1){
            System.err.println("App must be registered before benchmark");
        }
        account = new Account();
        account.start = System.currentTimeMillis();
        account.end = -1L;
        account.procTime = null;
        account.nLocalPktIn = 0;
        account.nRemotePktIn = 0;
        account.nRemotePktOut = 0;
    }

    public static void addLocalPktIn(){
        account.nLocalPktIn += 1;
    }

    public static void addRemotePktIn(){
        account.nRemotePktIn += 1;
    }

    public static void addRemotePktOut(){
        account.nRemotePktOut += 1;
    }

    public static String endBenchmark(){
        account.end = System.currentTimeMillis();
        long pid = Settings.getInstance().targetPid;
        if (pid == -1 || pid == 0){
            System.err.println(String.format("pid == %d", pid));
        }
        else {
            try {
                account.procTime = sigar.getProcTime(pid);
            } catch (SigarException e) {
                e.printStackTrace();
            }
        }
        return account.toString();
    }
}


class Account {
    Long start;
    Long end;

    ProcTime procTime;

    int nLocalPktIn;
    int nRemotePktIn;
    int nRemotePktOut;

    @Override
    public String toString(){
        return String.format(
                "[--benchmark--]\n" +
                        "Begin\t\t: %d\n" +
                        "End\t\t\t: %d\n" +
                        "During\t\t: %d\n" +
                        "Time\t\t: %s\n" +
                        "localPktIn\t: %d\n" +
                        "remotePktIn\t: %d\n" +
                        "remotePktOut: %d\n" +
                "[-- END of BENCHMARK --]\n",
                start,
                end,
                end - start,
                this.procTime.toString(),
                this.nLocalPktIn,
                this.nRemotePktIn,
                this.nRemotePktOut);
    }
}