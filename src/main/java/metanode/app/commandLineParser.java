package metanode.app;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Command Line Parser
 */

public class commandLineParser {

    /**
     * Logger
     */

    Logger logger = Logger.getLogger(commandLineParser.class.getName());


    /**
     * Parses the command line arguments
     *
     * @param args command line arguments
     * @return list of InetSocketAddresses
     */

    public List<InetSocketAddress> parse(String[] args) {
        if (args.length <= 1) {
            return null;
        } else {
            List<InetSocketAddress> addressList = new ArrayList<InetSocketAddress>();
            for (int i = 1; i < args.length; i++) {
                String[] parts = args[i].split(":");
                if (parts.length != 2) {
                    return null;
                }
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                InetSocketAddress address = new InetSocketAddress(host, port);
                addressList.add(address);
            }
            return addressList;
        }
    }
}
