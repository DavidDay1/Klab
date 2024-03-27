package klab.app;

import klab.serialization.BadAttributeValueException;
import klab.serialization.Message;
import klab.serialization.MessageInput;
import klab.serialization.MessageOutput;

import java.io.IOException;
import java.net.Socket;

public class Peer {
    private Socket socket;
    private MessageInput in;
    private MessageOutput out;
    private ThreadFunctions tf;

    public Peer(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new MessageInput(socket.getInputStream());
        this.out = new MessageOutput(socket.getOutputStream());
        this.tf = new ThreadFunctions();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public MessageInput getIn() {
        return in;
    }

    public MessageOutput getOut() {
        return out;
    }

}


