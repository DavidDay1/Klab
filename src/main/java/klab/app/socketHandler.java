package klab.app;

import java.net.ServerSocket;

import static klab.app.Node.logger;

public class socketHandler {
    private static socketHandler instance;

    private ServerSocket nodeSocket;
    private ServerSocket downloadSocket;

    public socketHandler() {}

    public static synchronized socketHandler getInstance() {
        if (instance == null) {
            instance = new socketHandler();
        }
        return instance;
    }

    public ServerSocket getNodeSocket() {
        logger.info("Node socket: " + this.nodeSocket);
        ServerSocket nodeSocket = this.nodeSocket;
        return nodeSocket;
    }

    public ServerSocket getDownloadSocket() {
        logger.info("Download socket: " + this.downloadSocket);
        ServerSocket newSocket = this.downloadSocket;
        return newSocket;
    }

    public void setNodeSocket(ServerSocket nodeSocket) {
        logger.info("Setting node socket: " + nodeSocket);
        this.nodeSocket = nodeSocket;
    }

    public void setDownloadSocket(ServerSocket downloadSocket) {
        logger.info("Setting download socket: " + downloadSocket);
        this.downloadSocket = downloadSocket;
    }
}
