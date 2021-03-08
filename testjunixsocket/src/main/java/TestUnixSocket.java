import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.Timestamp;

public class TestUnixSocket extends Thread {
    private static File socketFile = null;

    public static void main(String[] args) throws Exception {
        debug("=====start=====");
        debug("supports server or client params, default param is server");
        String dir = System.getProperty("sockdir", ".");
        if (dir == null || dir.length() == 0) {
            dir = ".";
        }
        socketFile = new File(dir + "testunixsocket.sock");

        boolean server = true;
        if (args != null && args.length == 1) {
            server = "server".equalsIgnoreCase(args[0]);
        }
        if (server) {
            startServer();
        }else {
            startClient();
        }

    }

    public static void startServer() throws Exception {
        socketFile.delete();
        AFUNIXServerSocket server = AFUNIXServerSocket.newInstance();
        server.bind(new AFUNIXSocketAddress(socketFile));
        debug("bind success, sockfile=" + socketFile);
        while (true) {
            Socket socket = server.accept();
            debug("accept a client");
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while (true) {
                String line = br.readLine();
                if (null == line) {
                    socket.close();
                    break;
                } else {
                    debug("accept data=" + line);
                }
            }
        }
    }
    public static void startClient() {
        int i = 0;
        while (true) {
            try {
                Thread.sleep(2000);
            } catch (Throwable e) {
            }
            AFUNIXSocket client = null;
            try {
                debug("start connect, use sockfile:" + socketFile.getAbsolutePath());
                client = AFUNIXSocket.connectTo(new AFUNIXSocketAddress(socketFile));
                debug("connected");
                while (true) {
                    i++;
                    client.getOutputStream().write(("msg" + i + "\n").getBytes());
                    debug("send message " + i);
                    Thread.sleep(1000);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                try {
                    client.close();
                } catch (Throwable ex) {
                }
            }
        }
    }

    private static void debug(Object o) {
        System.out.println(new Timestamp(System.currentTimeMillis()).toString() + " " + Thread.currentThread().getName() + " " + o);
    }
}
