package metanode.app;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * Socket Handler for MetANode
 */
public class socketHandler {

    /**
     * Instance of the socket handler
     */

    private static socketHandler instance;


    /**
     * Logger
     */

    private Logger logger = logHandler.getLogger();


    /**
     * Client Socket
     */

    private DatagramSocket clientSocket;

    /**
     * Port
     */

    private int port;

    /**
     * Address
     */

    private InetAddress inetAddress;

    public socketHandler() {}

    /**
     * Get the instance of the socket handler
     *
     * @return instance of the socket handler
     */
    public static synchronized socketHandler getInstance() {
        if (instance == null) {
            instance = new socketHandler();
        }
        return instance;
    }

    /**
     * Get the client socket
     *
     * @return client socket
     */

    public DatagramSocket getClientSocket() {
        logger.info("Client socket: " + this.clientSocket);
        DatagramSocket clientSocket = this.clientSocket;
        return clientSocket;
    }

    /**
     * Set the client socket
     *
     * @param clientSocket client socket
     */

    public void setClientSocket(DatagramSocket clientSocket) {
        logger.info("Setting client socket: " + clientSocket);
        this.clientSocket = clientSocket;
    }

    /**
     * Close the client socket
     */
    public void closeSocket() {
        logger.info("Closing client socket");
        this.clientSocket.close();
    }

    /**
     * Get the port
     *
     * @return port
     */

    public int getPort() {
        return this.port;
    }

    /**
     * Get the address
     *
     * @return address
     */

    public InetAddress getInetAddress() {
        return this.inetAddress;
    }

    /**
     * Set the port
     *
     * @param port port
     */

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Set the address
     *
     * @param inetAddress address
     */

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

}

