package metanode.app;


import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Represents a MetANode Client
 *
 * @version 1.0
 */

public class Client {

    /**
     * Logger for the client
     */

    public static final Logger logger = logHandler.getLogger();

    /**
     * Thread pool for the client
     */

    protected static final ExecutorService pool = Executors.newCachedThreadPool();


    /**
     * Socket handler for the client
     */

    protected static socketHandler sh = socketHandler.getInstance();


    /**
     * Timeout for the client socket
     */

    private static final int TIMEOUT = 3000;


    /**
     * Main method for the client
     *
     * @param args command line arguments
     * @throws UnknownHostException if the host is unknown
     * @throws SocketException      if the socket cannot be created
     */


    public static void main(String[] args) throws UnknownHostException, SocketException {
        if (args.length != 2) {
            System.err.println("Usage: <server(name or IP address) <port>");
            System.exit(1);
        }

        try {
            String server = args[0];
            int port = Integer.parseInt(args[1]);


            InetAddress serverAddress = InetAddress.getByName(server);

            if (!(serverAddress instanceof Inet4Address)) {
                System.err.println("Only IPv4 addresses are supported");
                System.exit(1);
            }

            DatagramSocket socket = new DatagramSocket();


            socket.setSoTimeout(TIMEOUT);
            sh = socketHandler.getInstance();
            sh.setClientSocket(socket);
            sh.setPort(port);
            sh.setInetAddress(serverAddress);


            commandLine cl = new commandLine();

            logger.info("Running command line interface");
            pool.submit(cl);


        } catch (SocketException e) {
            logger.severe("Unable to create socket");
            System.exit(1);
        } catch (UnknownHostException e) {
            logger.severe("Unknown host");
            System.err.println("Unknown host");
            System.exit(1);
        } catch (NumberFormatException e) {
            logger.severe("Invalid port number");
            System.err.println("Invalid port number: " + args[1]);
            System.exit(1);
        }
    }

}
