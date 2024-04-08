package klab.app;

import klab.serialization.MessageInput;
import klab.serialization.MessageOutput;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;

import static klab.app.Node.*;

/**
 * Class for handling connections
 * @version 1.0
 */

public class connectionHandler {

    /**
     * connectToPeer method for connecting to a peer
     *
     * @param args      arguments
     * @param directory directory
     */

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

    /**
     * listenForConnections method for listening for connections
     *
     * @param nodeSocket node socket
     * @param directory  directory
     * @return runnable
     */

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

    /**
     * establishConnection method for establishing a connection
     *
     * @param s         socket
     * @param directory directory
     * @throws IOException if I/O problem
     */


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

    /**
     * listenForDownload method for listening for downloads
     *
     * @param downloadSocket download socket
     * @param directory      directory
     * @return runnable
     */

    public Runnable listenForDownload(ServerSocket downloadSocket, File directory) {
        return () -> {
            while (!downloadSocket.isClosed()) {
                try {
                    logger.info("Uploading file: inside of listenForDownload");
                    Socket s = downloadSocket.accept();
                    logger.info("Download Connection accepted" + s.getInetAddress() + ":" + s.getPort());
                    OutputStream out = s.getOutputStream();
                    MessageInput in = new MessageInput(s.getInputStream());
                    String fileID = in.readString();
                    logger.info("Downloading file: " + fileID);
                    logger.info("Uploading file: inside of listenForDownload before thread");
                    DS.getExecutor().execute(DS.upload(out, fileID, s, directory));
                    logger.info("Uploading file: inside of listenForDownload after thread");
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Unable to communicate: ", e.getMessage());
                }
            }
        };
    }

    /**
     * downloadFile method for downloading a file
     *
     * @param args      arguments
     * @param directory directory
     */

    public void downloadFile(String[] args, File directory) {
        try {
            String peerIp = args[1];
            int peerPort = Integer.parseInt(args[2]);
            Socket s = new Socket(peerIp, peerPort);
            DS.getExecutor().execute(DS.download(args, s));

        } catch (SocketException e) {
            System.err.println("Invalid Download Port or Address: " + e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to communicate: ", e.getMessage());
        }
    }

}
