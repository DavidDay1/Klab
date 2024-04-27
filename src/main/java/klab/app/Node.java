package klab.app;

import klab.serialization.Result;
import klab.serialization.Search;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
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
     * Download service for downloading files
     */

    protected static final DownloadService DS = new DownloadService();


    /**
     * Message factory for generating messages
     */
    private static MessageFactory mf = MessageFactory.getInstance();

    /**
     * Get the message factory
     * @return the message factory
     */
    public static MessageFactory getMf() {
        return mf;
    }


    /**
     * Thread functions for handling threads
     */
    public static ThreadFunctions tf = new ThreadFunctions();

    /**
     * Logger for the node
     */

    public static final Logger logger = logHandler.getLogger();

    /**
     * Connection handler for handling connections
     */

    protected static connectionHandler ch = new connectionHandler();

    /**
     * Socket handler for handling sockets
     */

    protected static socketHandler socketHandler  = klab.app.socketHandler.getInstance();


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
        File directory = new File(args[1]);
        if (!directory.exists()) {
            System.err.println("Directory provided does not exist");
        }

        int nodePort = Integer.parseInt(args[0]);
        int downloadPort = Integer.parseInt(args[2]);


        try {
            ServerSocket nodeSocket = new ServerSocket(nodePort);

            ServerSocket downloadSocket = new ServerSocket(downloadPort);

            //established ServerSockets for the node and download ports

            socketHandler = klab.app.socketHandler.getInstance();

            socketHandler.setNodeSocket(nodeSocket);
            socketHandler.setDownloadSocket(downloadSocket);

            //initiate command line interface

            commandLine commandLine = new commandLine(directory);
            pool.submit(commandLine);

            //initiate listening for connections and downloads
            pool.submit(ch.listenForConnections(nodeSocket, directory));
            pool.submit(ch.listenForDownload(downloadSocket, directory));


        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to establish ports: " + args[0] + ":" + args[2]);
            System.exit(1);
        }


    }

}
