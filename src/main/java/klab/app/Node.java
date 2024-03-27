package klab.app;

import klab.serialization.Search;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

/**
 * Node class for the KLab network
 *
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
     * Message factory for generating messages
     */
    public static MessageFactory mf = new MessageFactory();


    /**
     * Thread functions for handling threads
     */
    public static ThreadFunctions tf = new ThreadFunctions();

    public static final Logger logger = logHandler.getLogger();

    /**
     * Directory for the node
     */
    public static File directory;

    protected static connectionHandler ch = new connectionHandler();

    /**
     * Main method for Node
     *
     * @param args command line arguments
     * @throws IOException if I/O problem
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: <local Node port> <local document directory> <local download port>");
            System.exit(1);
        }
        directory = new File(args[1]);
        if (!directory.exists()) {
            System.err.println("Directory provided does not exist");
        }

        int nodePort = Integer.parseInt(args[0]);
        int downloadPort = Integer.parseInt(args[2]);


        try {
            ServerSocket nodeSocket = new ServerSocket(nodePort);

            ServerSocket downloadSocket = new ServerSocket(downloadPort);

            mf.setMsgID();


            commandLine commandLine = new commandLine();
            pool.submit(commandLine);

            pool.submit(ch.listenForConnections(nodeSocket, Node.directory));


        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to establish ports: " + args[0] + ":" + args[2]);
            System.exit(1);
        }


    }

}
