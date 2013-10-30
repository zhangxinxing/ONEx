package ONExProtocol;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-30
 * Time: AM10:25
 */
public enum SQLiteHelper {
    SINGLETON_INSTANCE;

    public static final String T_HOSTS = "HOSTS";
    public static final String T_SWLINKS = "SWLINKS";
    public static final String T_FOREST = "FORESTS";
    public static final String SQLITE_DB_LOCALTOPO = "/tmp/localTopo.db";
    public static final String SQLITE_DB_GLOBALTOPO = "/tmp/GlobalTopo.db";

    public static final String CREATE_TABLES = String.format(
            "create table IF NOT EXISTS %s (" +
                    "dpid integer NOT NULL, " +
                    "port integer NOT NULL, " +
                    "ipv4 integer NOT NULL, " +
                    "MAC integer  NOT NULL PRIMARY KEY);" +
            "create table IF NOT EXISTS %s (" +
                    "src integer NOT NULL, " +
                    "srcPort integer NOT NULL, " +
                    "dst integer NOT NULL, " +
                    "dstPort integer NOT NULL," +
                    "UNIQUE (src, srcPort, dst, dstPort) ON CONFLICT IGNORE);" +
            "create table IF NOT EXISTS %s (" +
                    "controllerIP integer NOT NULL, " +
                    "controllerPort integer NOT NULL, " +
                    "dpid integer NOT NULL," +
                    "UNIQUE(controllerIP, controllerPort, dpid) ON CONFLICT IGNORE);",
            T_HOSTS, T_SWLINKS, T_FOREST);

    public static final String DROP_ALL = String.format(
            "drop table if exists %s;" +
            "drop table if exists %s;" +
            "drop table if exists %s;",
            T_HOSTS, T_SWLINKS, T_FOREST);

    public static final String INIT_ALL = DROP_ALL + CREATE_TABLES;



    private SQLiteHelper() {
    }

    public static SQLiteHelper getInstance() {
        return SINGLETON_INSTANCE;
    }

    public void init(){
    }


}
