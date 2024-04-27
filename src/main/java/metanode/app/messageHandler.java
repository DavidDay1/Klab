package metanode.app;

import metanode.serialization.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Message Handler
 * Handles sending and receiving messages
 */

public class messageHandler {

    /**
     * Maximum number of attempts to send a message
     */
    private static final int MAX_ATTEMPTS = 4;

    /**
     * Socket Handler
     */

    private static socketHandler sh = socketHandler.getInstance();

    /**
     * Logger
     */

    Logger logger = logHandler.getLogger();

    /**
     * Sends a message
     *
     * @param packet   packet to send
     * @param response whether or not to expect a response
     * @return Runnable
     * @throws IOException if error occurs
     */

    public Runnable sendMessage(DatagramPacket packet, boolean response) throws IOException {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            try {
                logger.info("Sending message: " + new Message(packet.getData()).getType().getCmd());
                sh.getClientSocket().send(packet);

                if (response) {
                    logger.info("Should receive an AR");
                    long startTime = System.currentTimeMillis();
                    long timeout = sh.getClientSocket().getSoTimeout();
                    boolean received = false;
                    Message m = null;


                    while (System.currentTimeMillis() < startTime + timeout) {
                        m = receiveMessage();
                        if (Objects.equals(m.getType().getCmd(), "AR")) {
                            if (MessageFactory.getSessionMap().get(packet) == m.getSessionID() || m.getSessionID() == 0) {
                                logger.info("Received message: " + m.getType().getCmd());
                                System.out.println(m.toString());
                                if (m.getError().getCode() != 0) {
                                    if (m.getError().getCode() == 10) {
                                        System.err.println("Error: System call failure");
                                    } else {
                                        System.err.println("Message Error: Incorrect packet received");
                                    }
                                }
                                received = true;
                                break;
                            } else {
                                System.err.println("Unexpected session ID: " + m.getSessionID());
                            }
                        } else {
                            System.err.println("Unexpected message type: " + m.getType().getCmd());
                        }
                    }
                    if (received) {
                        break;
                    }
                } else {
                    logger.info("No response expected");
                    break;
                }

            } catch (SocketTimeoutException e) {
                if (i == MAX_ATTEMPTS - 1) {
                    logger.info("Max attempts reached, unable to send message");
                }
                logger.info("Failed to send message, retrying with attempt " + (i + 1) + " of " + MAX_ATTEMPTS);
            }
        }
        return null;
    }

    /**
     * Removes zeros from Datagram padding
     *
     * @param buf byte array to remove zeros from
     * @return byte array
     */


    public byte[] removeZeros(byte[] buf, int len) {
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = buf[i];
        }
        return result;
    }

    /**
     * Receives a message
     *
     * @return Message
     * @throws IOException if error occurs
     */

    public Message receiveMessage() throws IOException {
        try {
            byte[] buf = new byte[1534];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            sh.getClientSocket().receive(packet);
            if (!Objects.equals(packet.getAddress(), sh.getInetAddress()) || packet.getPort() != sh.getPort()) {
                throw new IOException("Received packet from unknown source: " + packet.getAddress() + ":" + packet.getPort());
            }
            int len = packet.getLength();
            byte[] data = removeZeros(packet.getData(), len);
            Message m = new Message(data);
            logger.info("Received message: " + m.getType().getCmd());
            return m;
        } catch (SocketTimeoutException e) {
            throw new SocketTimeoutException("Timeout reached, no message received");
        } catch (IOException e) {
            System.err.println("Communication error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid message: " + e.getMessage());
        }
        return null;
    }

}
