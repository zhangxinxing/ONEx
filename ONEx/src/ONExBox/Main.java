package ONExBox;

import ONExBox.gateway.Gateway;
import ONExProtocol.SQLiteHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created with IntelliJ IDEA.
 * User: Fan
 * Date: 13-10-31
 * Time: AM11:55
 */
public class Main {
    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Wrong parameters: DAEMON_PORT SERVER_PORT");
            for (String s : args) {
                System.err.println(s);
            }
            System.exit(-1);
        }

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + SQLiteHelper.SQLITE_DB_GLOBALTOPO);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(5);
            statement.executeUpdate(SQLiteHelper.INIT_ALL);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                System.err.println(e);
            }
        }

        int DAEMON_PORT = Integer.parseInt(args[0]);
        int SERVER_PORT = Integer.parseInt(args[1]);

        ONExSetting.getInstance().setNetworkConfig(DAEMON_PORT, SERVER_PORT);

        new Gateway(DAEMON_PORT, SERVER_PORT);
    }
}
