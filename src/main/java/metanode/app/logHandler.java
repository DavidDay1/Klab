package metanode.app;

import java.io.IOException;
import java.util.logging.*;

/**
 * Logger for MetANode
 */

public class logHandler {
    protected static final Logger logger = Logger.getLogger("metanode.app");

    static {
        try {
            logger.setUseParentHandlers(false);
            for (Handler h : logger.getHandlers()) {
                logger.removeHandler(h);
            }

            Handler h = new FileHandler("metanode.log");
            h.setFormatter(new SimpleFormatter());
            h.setLevel(Level.ALL);

            logger.addHandler(h);
        } catch (SecurityException | IOException e) {
            System.err.println("Unable to create file handler");
            System.exit(1);
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}
