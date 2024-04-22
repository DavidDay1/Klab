package metanode.app;

import metanode.serialization.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

public class messageHandler {
    private static final int MAX_ATTEMPTS = 4;

    private static socketHandler sh = socketHandler.getInstance();

    Logger logger = Logger.getLogger(messageHandler.class.getName());

    public Runnable sendMessage(DatagramPacket packet, boolean response) throws IOException {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            try {
                logger.info("Sending message: " + new Message(packet.getData()).getType().getCmd());
                sh.getClientSocket().send(packet);

                if (response) {
                    Message m = receiveMessage();
                    if (Objects.equals(m.getType().getCmd(), "AR")) {
                        logger.info("Received message: " + m.getType().getCmd());
                        System.out.println(m.toString());
                        break;
                    } else {
                        System.err.println("Unexpected message type: " + m.getType().getCmd());
                    }
                }

            } catch (SocketTimeoutException e) {
                if (i == MAX_ATTEMPTS - 1) {
                    logger.info("Max attempts reached, unable to send message");
                }
                logger.info("Failed to send message, retrying with attempt " + (i + 1) + " of " + MAX_ATTEMPTS);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }
        return null;
    }


    public byte[] removeZeros(byte[] buf) {
        int i = buf.length - 1;

        while (buf[i] == 0) {
            --i;
        }

        if (i < 4) {
            byte result[] = new byte[4];
        }
        byte[] result = new byte[i + 1];
        System.arraycopy(buf, 0, result, 0, i + 1);

        return result;
    }

    public Message receiveMessage() throws IOException {
        try {
            byte[] buf = new byte[1534];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            sh.getClientSocket().receive(packet);
            if (!Objects.equals(packet.getAddress(), sh.getInetAddress()) || packet.getPort() != sh.getPort()) {
                throw new IOException("Received packet from unknown source: " + packet.getAddress() + ":" + packet.getPort());
            }
            byte[] data = removeZeros(packet.getData());
            Message m = new Message(data);
            logger.info("Received message: " + m.getType().getCmd());
            return m;
        } catch (IOException e) {
            System.err.println("Communication error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid message: " + e.getMessage());
        }
        return null;
    }

}
