package klab.app;

import klab.serialization.MessageOutput;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import static klab.app.Node.logger;

public class commandLine implements Runnable {
    private final File directory;

    public commandLine(File directory) {
        this.directory = directory;
    }

    @Override
    public void run() {
        Scanner user = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String command = user.next();
            switch (command) {
                case "exit":
                    for (Peer p : Node.peerList) {
                        p.close();
                    }
                    System.exit(0);
                    break;
                case "connect":
                    logger.info("Connecting with directory " + this.directory);
                    String[] args = user.nextLine().split(" ");
                    if (args.length != 3) {
                        System.err.println("Bad Connect command: Expect connect <ip> <port>");
                    } else {
                        Node.ch.connectToPeer(args, this.directory);
                    }
                    break;
                case "download":
                    //TODO: Implement download
                    break;
                default:
                    for (Peer p : Node.peerList) {
                            try {
                                logger.info("Searching peer: " + p.getSocket().getInetAddress() + ":" + p.getSocket().getPort() + " to search for " + command);
                                Node.pool.submit(Node.tf.handleOutSearch(command, p.getSocket(),
                                        new MessageOutput(p.getSocket().getOutputStream()),
                                        Node.getMf(), Node.searchList));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    break;
            }

        }
    }
}
