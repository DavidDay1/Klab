package klab.app;

import java.net.ServerSocket;

import static klab.app.Node.logger;

/**
 * Class for handling sockets
 * @version 1.0
 */

public class socketHandler {
    private static socketHandler instance;

    private ServerSocket nodeSocket;
    private ServerSocket downloadSocket;


    /**
     * Constructor for the socketHandler
     */
    public socketHandler() {}

    /**
     * Method for getting the instance of the socketHandler
     * @return instance
     */

    public static synchronized socketHandler getInstance() {
        if (instance == null) {
            instance = new socketHandler();
        }
        return instance;
    }


    /**
     * Method for getting the node socket
     * @return node socket
     */
    public ServerSocket getNodeSocket() {
        logger.info("Node socket: " + this.nodeSocket);
        ServerSocket nodeSocket = this.nodeSocket;
        return nodeSocket;
    }

    /**
     * Method for getting the download socket
     * @return download socket
     */

    public ServerSocket getDownloadSocket() {
        logger.info("Download socket: " + this.downloadSocket);
        ServerSocket newSocket = this.downloadSocket;
        return newSocket;
    }

    /**
     * Method for setting the node socket
     * @param nodeSocket node socket
     */

    public void setNodeSocket(ServerSocket nodeSocket) {
        logger.info("Setting node socket: " + nodeSocket);
        this.nodeSocket = nodeSocket;
    }

    /**
     * Method for setting the download socket
     * @param downloadSocket download socket
     */
    public void setDownloadSocket(ServerSocket downloadSocket) {
        logger.info("Setting download socket: " + downloadSocket);
        this.downloadSocket = downloadSocket;
    }
}
