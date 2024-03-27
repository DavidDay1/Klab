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

    public void connectToPeer(Scanner user, File directory) {
        try {
            if (!user.hasNext()) {
                System.err.println("usage connect <ip> <port>");
                return;
            }
            String peerIp = user.next();
            int peerPort = Integer.parseInt(user.next());

            Socket s = new Socket(peerIp, peerPort);
            synchronized (peerList) {
                peerList.add(new Peer(s));
            }
            System.out.println("Connected to peer: " + peerIp + ":" + peerPort + " Peer List Size: " + peerList.size());
            pool.submit(() -> tf.handleIn(new MessageInput(s.getInputStream()),
                    new MessageOutput(s.getOutputStream()), s, directory,
                    Node.searchList, new InetSocketAddress(s.getInetAddress(), s.getPort())));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to communicate: ", e.getMessage());
        }
    }

    public Runnable listenForConnections(ServerSocket nodeSocket, File directory) {
        return () -> {
            while (!nodeSocket.isClosed()) {
                try {
                    Socket s = nodeSocket.accept();
                    synchronized (peerList) {
                        peerList.add(new Peer(s));
                    }
                    pool.submit(() -> tf.handleIn(new MessageInput(s.getInputStream()),
                            new MessageOutput(s.getOutputStream()), s, directory, Node.searchList,
                            new InetSocketAddress(s.getInetAddress(), s.getPort())));
                    System.out.println("Connected to peer: " + s.getInetAddress() + ":" + s.getPort() + " Peer List " +
                            "Size: " + peerList.size());
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Unable to communicate: ", e.getMessage());
                }
            }
        };
    }
}
