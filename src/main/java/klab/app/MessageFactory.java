package klab.app;

import klab.serialization.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Class for generating messages
 * @version 1.0
 */

public class MessageFactory {
    /**
     * Message ID
     */
    private byte[] msgID = new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    private MessageFactory(){
        setMsgID();
    }

    public static synchronized MessageFactory getInstance(){
        return new MessageFactory();
    }

    /**
     * Set the message ID to a random value
     */

    public void setMsgID() {
        for (int i = 0; i < this.msgID.length; i++) {
            this.msgID[i] = (byte) (Math.random() * 255);
        }
    }

    /**
     * Generate a message ID
     * @return message ID
     */

    public byte[] generateMsgID() {
        byte[] msgID = this.msgID;
        //um id is changing every other byte change to increment better dummy
        int i = msgID.length - 1;
        while (i >= 0) {
            if (this.msgID[i] == 255) {
                this.msgID[i] = 0;
                i--;
                break;
            } else {
                this.msgID[i]++;
                break;
            }
        }
        return msgID;
    }

    /**
     * Generate a time to live
     * @return time to live
     */

    public int generateTTL() {
        return 10;
    }

    /**
     * Generate a routing service
     * @return routing service
     */

    public RoutingService generateRoutingService() {
        return RoutingService.DEPTHFIRST;
    }

    /**
     * Generate a search message
     * @param r response Message
     * @param s search Message
     * @return message containing search and response result
     */

    public static String printMessage(Search s, Response r) {
        String message = "";
        message += "Search Response for " + s.getSearchString() + ":\nDownload host: " + r.getResponseHost() + "\n";
        //System.out.println("Search Response for " + s.getSearchString() + ":\nDownload host: " + r.getResponseHost());
        for (Result result : r.getResultList()) {
            //System.out.println("\t" + result.getFileName() + ": ID " + String.format("%X", ByteBuffer.wrap(result.getFileID()).getInt()) + "(" + result.getFileSize() + " bytes)");
            message += "\t" + result.getFileName() + ": ID " + String.format("%X", ByteBuffer.wrap(result.getFileID()).getInt()) + "(" + result.getFileSize() + " bytes)\n";
        }
        //System.out.print("> ");
        message += "> ";
        return message;
    }

    /**
     * Generate results for a response
     * @param r response message
     * @param files list of files
     * @throws BadAttributeValueException if file ID is invalid
     */

    public static void generateResults(Response r, List<File> files) throws BadAttributeValueException {
        for (File f : files) {
            byte[] fileID = ByteBuffer.allocate(4).putInt(f.hashCode()).array();
            Result result = new Result(fileID, f.length(), f.getName());
            r.addResult(result);
        }
    }
}