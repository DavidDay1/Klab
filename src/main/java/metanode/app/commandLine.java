package metanode.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Command Line Interface for MetANode
 *
 * @version 1.0
 */

public class commandLine implements Runnable {

    /**
     * Command Line Parser
     */

    private static commandLineParser clp = new commandLineParser();

    /**
     * Message Factory
     * generates Datgrams based on messages
     */

    private static MessageFactory mf = new MessageFactory();

    /**
     * Message Handler
     * sends and receives messages
     */

    private static messageHandler mh = new messageHandler();

    /**
     * Logger
     */

    Logger logger = Logger.getLogger(commandLine.class.getName());


    /**
     * Run method for the command line
     * handles inputs from the users
     */

    @Override
    public void run() {
        Scanner user = new Scanner(System.in);
        while (true) {
            try {
                System.out.print("> ");
                String command = user.next();
                switch (command) {
                    case "RN", "RM":
                        Client.pool.submit(mh.sendMessage(mf.createMessage(command), true));
                        break;
                    case "NA", "MA", "ND", "MD":
                        String[] args = user.nextLine().split(" ");
                        if (args.length > 1) {
                            List<InetSocketAddress> data = clp.parse(args);
                            Client.pool.submit(mh.sendMessage(mf.createMessage(command, data), false));
                        } else {
                            System.err.println(command + " command expects at least one argument: " + command);
                        }
                        break;
                    case "exit":
                        Client.pool.shutdownNow();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Available Commands: RN, RM, NA, MA, ND, MD, exit");
                        break;
                }
            } catch (UnknownHostException e) {
                logger.info("Unknown Host Exception: " + e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (RuntimeException e) {
                logger.info("Runtime Exception: " + e.getMessage());
            }
        }
    }
}
