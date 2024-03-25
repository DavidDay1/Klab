package klab.app;

import klab.serialization.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
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

    /**
     * List of searches
     */
    protected static HashMap<String, Search> searchList = new HashMap<String, Search>();

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
    protected static MessageFactory mf = new MessageFactory();

    /**
     * Thread functions for handling threads
     */
    private static ThreadFunctions tf = new ThreadFunctions();


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
     * Main method for Node
     * @param args command line arguments
     * @throws IOException if I/O problem
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: <doc> [<neighbor ip/name> <neighbor port>");
            System.exit(1);
        }
        File directory = new File(args[0]);
        if (!directory.exists()) {
            System.err.println("Directory provided does not exist");
        }

        String neighbor = args[1];
        int port = Integer.parseInt(args[2]);

        try {
            //try to connect to neighbor
            Socket s = new Socket(neighbor, port);

            //initialize node's local address and ports
            InetSocketAddress responseHost = new InetSocketAddress(s.getLocalAddress(), s.getLocalPort());

            //initialize input and output streams
            MessageInput in = new MessageInput(s.getInputStream());
            MessageOutput out = new MessageOutput(s.getOutputStream());

            mf.setMsgID();

            logger.info("Connected to neighbor at: " + neighbor + ":" + port);
            Scanner user = new Scanner(System.in);

            pool.submit(tf.handleOutSearch(user, s, out, mf, searchList));
            pool.submit(tf.handleIn(in, out, s, directory, searchList, responseHost));




        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to communicate: " + args[1] + ":" + args[2]);
            System.exit(1);
        }


    }

}
