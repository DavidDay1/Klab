package klab.app;

import klab.serialization.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

import static klab.app.Node.logger;
import static klab.app.Node.pool;

/**
 * Class for handling threads
 * @version 1.0
 */

public class ThreadFunctions {

    /**
     * Handle the out search
     * @param user user input
     * @param s socket
     * @param out message output
     * @param mf message factory
     * @param searchList list of searches
     * @return runnable
     */

    public Runnable handleOutSearch(Scanner user, Socket s, MessageOutput out, MessageFactory mf, HashMap<String,
            Search> searchList) {
        return () -> {
            while (s.isConnected()) {
                try {
                    System.out.print("> ");
                    String search = user.nextLine();
                    if (search.equals("exit")) {
                        logger.info("Disconnecting from neighbor");
                        user.close(); 
                        s.close();
                        pool.shutdownNow();
                        System.exit(0);
                        break;
                    }
                    logger.info("Sent search: " + search);
                    Search searchMessage = new Search(mf.generateMsgID(), mf.generateTTL(), mf.generateRoutingService(), search);
                    searchMessage.encode(out);
                    searchList.put(Arrays.toString(searchMessage.getID()), searchMessage);
                } catch (BadAttributeValueException e) {
                    logger.log(Level.WARNING, "Invalid message: " + e.getMessage());
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Unable to communicate: ", e.getMessage());
                }
            }
        };
    }

    /**
     * Handle the in messages
     * @param in message input
     * @param out message output
     * @param s socket
     * @param directory directory
     * @param searchList list of searches
     * @param responseHost response host
     * @return runnable
     */

    public Runnable handleIn(MessageInput in, MessageOutput out, Socket s, File directory,
                              HashMap<String, Search> searchList, InetSocketAddress responseHost) {
        return () -> {
            while (s.isConnected()) {
                try {
                    Message m;
                    synchronized (in) {
                        m = Message.decode(in);
                    }
                    if (m instanceof Response) {
                        pool.submit(new ThreadFunctions().handleResponse(m, searchList));
                    } else if (m instanceof Search) {
                        pool.submit(new ThreadFunctions().handleSearch(m, out, s, directory, responseHost));
                    }
                } catch (IOException e) {
                    if (s.isClosed()) {
                        logger.info("Disconnected from neighbor");
                        pool.shutdownNow();
                        break;
                    }
                    logger.log(Level.SEVERE, "Unable to communicate: " + e.getMessage());
                } catch (BadAttributeValueException e) {
                    logger.log(Level.WARNING, "Invalid message: " + e.getMessage());
                }

            }
        };
    }

    /**
     * Handle the search in
     * @param m message
     * @param out message output
     * @param s socket
     * @param directory directory
     * @param responseHost response host
     * @return runnable
     */

    public Runnable handleSearch(Message m, MessageOutput out, Socket s, File directory,
                                  InetSocketAddress responseHost) {
        return () -> {
            try {
            //confirming message type
            Search search = (Search) m;
            logger.info("Received search: " + search);

            //creating a response
            Response response = new Response(m.getID(), m.getTTL()-1, m.getRoutingService(), responseHost);

            //user looks for "" don't look for files
            if (search.getSearchString().isEmpty()) {
                synchronized (out) {
                    response.encode(out);
                }
            } else {
                //check files with matching search string
                List<File> results = FileSearch.search(directory, search.getSearchString());

                if (!results.isEmpty()) {
                    //if there are files that exist with matching search string
                    //send response with results
                    MessageFactory.generateResults(response, results);
                    logger.info("Sending response: " + response + " to " + s.getRemoteSocketAddress() + " for search:" +
                            " " + search.getSearchString());
                    synchronized (out) {
                        response.encode(out);
                    }
                    logger.log(Level.INFO, "Sending response: " + response + " to "
                            + s.getRemoteSocketAddress() + " for search: " + search.getSearchString());
                } else {
                    logger.info("No files found for Search: " + search.getSearchString());
                }
            }
        } catch (IOException e) {
                if (s.isClosed()) {
                    logger.info("Disconnected from neighbor");
                    pool.shutdownNow();
                }
                logger.log(Level.SEVERE, "Unable to communicate: " + e.getMessage());
            } catch (BadAttributeValueException e) {
                logger.log(Level.WARNING, "Invalid message: " + e.getMessage());
            }
        };
    }


    /**
     * Handle the response in
     * @param m message
     * @param searchList list of searches
     * @return runnable
     */


    public Runnable handleResponse(Message m, HashMap<String, Search> searchList) {
        return () -> {
                Response r = (Response) m;

                //look for search matching response
                Search search = searchList.get(Arrays.toString(r.getID()));
                if (search == null) {
                    logger.log(Level.INFO, "Received response with no matching search: " + r);
                    return;
                }
                logger.log(Level.INFO, "Received response message " + r);
                logger.log(Level.INFO, "Received response for search: " + search.getSearchString() + " from " + r.getResponseHost());
                System.out.print(MessageFactory.printMessage(search, r));
        };
    }
}
