package klab.app;

import klab.serialization.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

/**
 * Node class for the KLab network
 * @version 1.0
 */

public class Node {
    public ServerSocket nodeSocket;

    public ServerSocket downloadSocket;


    /**
     * List of searches
     */
    protected static HashMap<String, Search> searchList = new HashMap<String, Search>();

    /**
     * List of peers
     */

    protected static List<Peer> peerList = new ArrayList<Peer>();

    /**
     * Thread pool for handling threads
     */
    protected static final ExecutorService pool = Executors.newCachedThreadPool();

    /**
     * Logger for Node
     */
    protected static final Logger logger = Logger.getLogger("klab.app.Node");

    /**
     * Message factory for generating messages
     */
    public static MessageFactory mf = new MessageFactory();

    /**
     * Thread functions for handling threads
     */
    public static ThreadFunctions tf = new ThreadFunctions();


    static {
        try {
            logger.setUseParentHandlers(false);
            for (Handler h : logger.getHandlers()) {
                logger.removeHandler(h);
            }


            Handler h = new FileHandler("node.log");
            h.setFormatter(new SimpleFormatter());
            h.setLevel(Level.ALL);

            Handler ch = new ConsoleHandler();
            ch.setFormatter(new SimpleFormatter());
            ch.setLevel(Level.WARNING);


            logger.addHandler(h);
            logger.addHandler(ch);
        } catch (SecurityException | IOException e) {
            System.err.println("Unable to create file handler");
            System.exit(1);
        }
    }

    /**
     * Establish Peer Connection
     */

    public static void connectToPeer(Scanner user) {
        while (true) {
            try {
                String peerIp = user.next();
                int peerPort = Integer.parseInt(user.next());

                Socket s = new Socket(peerIp, peerPort);
                peerList.add(new Peer(s));
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Unable to communicate: ", e.getMessage());
            }
        }
    }

    public static void listenForConnections(ServerSocket nodeSocket, File directory,
                                            HashMap<String, Search> searchList) {
        while (true) {
            try {
                Socket s = nodeSocket.accept();
                peerList.add(new Peer(s));
                pool.submit(() -> tf.handleIn(new MessageInput(s.getInputStream()),
                        new MessageOutput(s.getOutputStream()), s, directory, searchList,
                        new InetSocketAddress(s.getInetAddress(), s.getPort())));
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Unable to communicate: ", e.getMessage());
            }
        }
    }




    /**
     * Main method for Node
     * @param args command line arguments
     * @throws IOException if I/O problem
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: <local Node port> <local document directory> <local download port>");
            System.exit(1);
        }
        File directory = new File(args[1]);
        if (!directory.exists()) {
            System.err.println("Directory provided does not exist");
        }

        int nodePort = Integer.parseInt(args[0]);
        int downloadPort = Integer.parseInt(args[2]);


        try {
            Node node = new Node();
            ServerSocket nodeSocket = new ServerSocket(nodePort);

            ServerSocket downloadSocket = new ServerSocket(downloadPort);

            commandLine commandLine = new commandLine();
            pool.submit(() -> commandLine.run());

            pool.submit(() -> listenForConnections(nodeSocket, directory, searchList));








        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to establish ports: " + args[0] + ":" + args[2]);
            System.exit(1);
        }


    }

}
