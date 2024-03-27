package klab.app;

import klab.serialization.MessageOutput;

import java.io.IOException;
import java.util.List;
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
                    for (Peer p : Node.peerList) {
                        p.close();
                    }
                    System.exit(0);
                    break;
                case "connect":
                    Node.ch.connectToPeer(user, Node.directory);
                    break;
                case "download":
                    //TODO: Implement download
                    break;
                default:
                    System.out.println(Node.peerList.size());
                    for (Peer p : Node.peerList) {
                            try {
                                System.out.println("Searching peer: " + p.getSocket().getInetAddress() + ":" + p.getSocket().getPort() + "to search for " + command);
                                Node.pool.submit(Node.tf.handleOutSearch(command, p.getSocket(),
                                        new MessageOutput(p.getSocket().getOutputStream()),
                                        Node.mf, Node.searchList));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    break;
            }

        }
    }
}
