package klab.app;

import klab.serialization.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import static klab.app.Node.*;

/**
 * Class for handling threads
 *
 * @version 1.0
 */

public class ThreadFunctions {

    /**
     * Handle the out search
     *
     * @param command    search value
     * @param s          socket
     * @param out        message output
     * @param mf         message factory
     * @param searchList list of searches
     * @return runnable
     */

    public Runnable handleOutSearch(String command, Socket s, MessageOutput out, MessageFactory mf, HashMap<String,
            Search> searchList)  {
        return () -> {
            try {
                logger.info("Searching for: " + command);
                Search searchMessage = new Search(mf.generateMsgID(), mf.generateTTL(),
                        mf.generateRoutingService(), command);
                synchronized (out) {
                    searchMessage.encode(out);
                }
                searchList.put(Arrays.toString(searchMessage.getID()), searchMessage);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Unable to communicate: ", e.getMessage());
            } catch (BadAttributeValueException e) {
                System.err.println("Unable to send search request: " + e.getMessage());
                System.out.print("> ");
            }
        };
    }

    /**
     * Handle the in messages
     *
     * @param in           message input
     * @param out          message output
     * @param s            socket
     * @param directory    directory
     * @param searchList   list of searches
     * @param responseHost response host
     * @return runnable
     */

    public Runnable handleIn(MessageInput in, MessageOutput out, Socket s, File directory,
                             HashMap<String, Search> searchList, InetSocketAddress responseHost) {
        return () -> {
            Message m;
            while (s.isConnected()) {
                try {
                    logger.info("Waiting for message");
                    synchronized (in) {
                        m = Message.decode(in);
                    }
                    m.setTTL(m.getTTL() - 1);

                    if (m.getTTL() < 1) {
                        logger.info("Message TTL expired: " + m);
                        continue;
                    }

                    logger.info("Received message: " + m);
                    if (m instanceof Response) {
                        logger.info("Processing response" + m);
                        pool.submit(new ThreadFunctions().handleResponse(m, s, searchList, Node.getMf()));
                    } else if (m instanceof Search) {
                        logger.info("Processing search" + m);
                        pool.submit(new ThreadFunctions().handleSearch(m, out, s, directory, responseHost, Node.getMf()));
                    }
                } catch (IOException e) {
                    logger.info("Disconnected from neighbor " + e.getMessage());
                    try {
                        s.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    pool.shutdownNow();
                    break;
                } catch (BadAttributeValueException e) {
                    logger.log(Level.WARNING, "Invalid message: " + e.getMessage());
                }
            }

        };
    }

    /**
     * Handle the search in
     *
     * @param m            message
     * @param out          message output
     * @param s            socket
     * @param directory    directory
     * @param responseHost response host
     * @return runnable
     */

    public Runnable handleSearch(Message m, MessageOutput out, Socket s, File directory,
                                 InetSocketAddress responseHost, MessageFactory mf) {
        return () -> {
            try {
                //confirming message type
                Search search = (Search) m;

                logger.info("Received search: " + search);

                for (Peer p : peerList) {
                    if (!p.getSocket().equals(s)) {
                        logger.info("Forwarding search: " + search + " to " + p.getSocket().getRemoteSocketAddress());
                        m.encode(p.getOut());
                    }
                }

                klab.app.socketHandler socketHandler = klab.app.socketHandler.getInstance();

                ServerSocket downloadSocket = socketHandler.getDownloadSocket();

                //creating a response
                Response response = new Response(m.getID(), m.getTTL(), m.getRoutingService(), new InetSocketAddress(s.getInetAddress(), downloadSocket.getLocalPort()));
                logger.info("Created response: " + response + " to " + s.getRemoteSocketAddress() + " for search: " +
                        search.getSearchString());


                //user looks for "" don't look for files
                if (search.getSearchString().isEmpty()) {
                    logger.info("Search String '' received sending " + response);
                    synchronized (out) {
                        response.encode(out);
                    }
                } else {
                    //check files with matching search string
                    List<File> results = FileSearch.searchByName(directory, search.getSearchString());
                    logger.info("Searching for files with search string: " + search.getSearchString());

                    if (!results.isEmpty()) {
                        //if there are files that exist with matching search string
                        //send response with results
                        mf.generateResults(response, results);
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
     *
     * @param m          message
     * @param searchList list of searches
     * @return runnable
     */


    public Runnable handleResponse(Message m, Socket s, HashMap<String, Search> searchList, MessageFactory mf) {
        return () -> {
            Response r = (Response) m;


            //look for search matching response
            Search search = searchList.get(Arrays.toString(r.getID()));
            if (search == null) {
                logger.log(Level.INFO, "Received response with no matching search: " + r);
                for (Peer p : peerList) {
                    if (!p.getSocket().equals(s)) {
                        synchronized (p.getOut()) {
                            try {
                                r.encode(p.getOut());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                return;
            }
            logger.log(Level.INFO, "Received response message " + r);
            logger.log(Level.INFO, "Received response for search: " + search.getSearchString() + " from " + r.getResponseHost());
            System.out.print(mf.printMessage(search, r));
        };
    }
}
