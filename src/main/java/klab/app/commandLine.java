package klab.app;

import klab.serialization.MessageOutput;

import java.io.IOException;
import java.util.Scanner;

import static klab.app.Node.logger;

public class commandLine implements Runnable {

    @Override
    public void run() {
        Scanner user = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String command = user.next();
            switch (command) {
                case "exit":
                    System.out.println("Command should be exit was: " + command);
                    //logger.info("Exiting from command line...");
                    for (Peer p : Node.peerList) {
                        p.close();
                    }
                    System.exit(0);
                    break;
                case "connect":
                    System.out.println("Command should be connect was: " + command);
                    //logger.info("Connecting to peer...");
                    Node.connectToPeer(user);
                case "download":
                    //TODO: Implement download
                default:
                    System.out.println("Command should be search was: " + command);
                    if (Node.peerList.isEmpty()) {
                        System.out.println("No peers to search");
                    } else {
                        for (Peer p : Node.peerList) {
                            try {
                                Node.pool.submit(Node.tf.handleOutSearch(command, p.getSocket(),
                                        new MessageOutput(p.getSocket().getOutputStream()),
                                        Node.mf, Node.searchList));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
            }

        }
    }
}
