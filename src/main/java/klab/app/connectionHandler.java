package klab.app;

import klab.serialization.MessageInput;
import klab.serialization.MessageOutput;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;

import static klab.app.Node.*;

public class connectionHandler {

    public void connectToPeer(String[] args, File directory) {
        try {
            String peerIp = args[1];
            int peerPort = Integer.parseInt(args[2]);

            Socket s = new Socket(peerIp, peerPort);
            establishConnection(s, directory);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to communicate: ", e.getMessage());
        }
    }

    public Runnable listenForConnections(ServerSocket nodeSocket, File directory) {
        return () -> {
            while (!nodeSocket.isClosed()) {
                try {
                    Socket s = nodeSocket.accept();
                    establishConnection(s, directory);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Unable to communicate: ", e.getMessage());
                }
            }
        };
    }

    public void establishConnection(Socket s, File directory) throws IOException {
        synchronized (peerList) {
            peerList.add(new Peer(s));
        }
        pool.submit(tf.handleIn(new MessageInput(s.getInputStream()),
                new MessageOutput(s.getOutputStream()), s, directory, Node.searchList,
                new InetSocketAddress(s.getInetAddress(), s.getPort())));
        logger.info("Connected to peer: " + s.getInetAddress() + ":" + s.getPort() + " Peer List " +
                "Size: " + peerList.size());
    }
}
