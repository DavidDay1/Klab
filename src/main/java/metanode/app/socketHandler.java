package metanode.app;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

public class socketHandler {
    private static socketHandler instance;

    private Logger logger = logHandler.getLogger();

    private DatagramSocket clientSocket;
    private int port;
    private InetAddress inetAddress;

    public socketHandler() {}

    public static synchronized socketHandler getInstance() {
        if (instance == null) {
            instance = new socketHandler();
        }
        return instance;
    }

    public DatagramSocket getClientSocket() {
        logger.info("Client socket: " + this.clientSocket);
        DatagramSocket clientSocket = this.clientSocket;
        return clientSocket;
    }

    public void setClientSocket(DatagramSocket clientSocket) {
        logger.info("Setting client socket: " + clientSocket);
        this.clientSocket = clientSocket;
    }

    public void closeSocket() {
        logger.info("Closing client socket");
        this.clientSocket.close();
    }

    public int getPort() {
        return this.port;
    }

    public InetAddress getInetAddress() {
        return this.inetAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

}

