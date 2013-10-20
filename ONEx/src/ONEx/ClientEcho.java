package ONEx;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * User: fan
 * Date: 13-10-9
 * Time: PM3:51
 */
public class ClientEcho {
    private static Logger log = Logger.getLogger(ClientEcho.class);

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(12345, 1);
            System.out.println("Listening for connections on port "
                    + server.getLocalPort());
            while (true) {
                Socket connection = server.accept();
                log.info("Connection established with " + connection);
                Thread input = new InputThread(connection.getInputStream());
                input.start();

                Thread output = new OutputThread(connection.getOutputStream());
                output.start();
                try {
                    input.join();
                    output.join(); }
                catch (InterruptedException ex) {
                    ex.printStackTrace();
                    break;
                }
                finally {
                    connection.close();
                }
            } }
        catch (IOException ex) {
            ex.printStackTrace( );
        }
    }
}

class InputThread extends Thread {
    InputStream in;

    public InputThread(InputStream in) {
        this.in = in;
    }

    public void run() {
        try {
            while (true) {
                int i = in.read();
                if (i == -1)
                    break;
                System.out.println(i);
            }
        }
        catch (SocketException ex) {
            // output thread closed the socket
        }
        catch (IOException ex) {
            System.err.println(ex);
        }
        try {
            in.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}

class OutputThread extends Thread {
    private Writer out;

    public OutputThread(OutputStream out) {
        this.out = new OutputStreamWriter(out);
    }

    public void run() {
        String line;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (true) {
                line = in.readLine();
                if (line != null && line.equals("."))
                    break;
                out.write(line +"\r\n"); out.flush( );
            }
        }catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            out.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
