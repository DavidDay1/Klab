package klab.app;

import klab.serialization.BadAttributeValueException;
import klab.serialization.Message;
import klab.serialization.MessageInput;
import klab.serialization.MessageOutput;

import java.io.IOException;
import java.net.Socket;

/**
 * Peer class for the KLab network
 *
 * @version 1.0
 */
public class Peer {
    private Socket socket;
    private MessageInput in;
    private MessageOutput out;
    private ThreadFunctions tf;


    /**
     * Constructor for the Peer class
     * @param socket socket
     * @throws IOException exception
     */
    public Peer(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new MessageInput(socket.getInputStream());
        this.out = new MessageOutput(socket.getOutputStream());
        this.tf = new ThreadFunctions();
    }


    /**
     * Method for sending a message
     */
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * get the socket
     * @return socket
     */

    public Socket getSocket() {
        return socket;
    }

    /**
     * get the MessageInput
     * @return MessageInput
     */

    public MessageInput getIn() {
        return in;
    }

    /**
     * get the MessageOutput
     * @return MessageOutput
     */

    public MessageOutput getOut() {
        return out;
    }

}


