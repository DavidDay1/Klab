package metanode.app;

import metanode.serialization.ErrorType;
import metanode.serialization.Message;
import metanode.serialization.MessageType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * Message Factory
 * Generates messages
 */

public class MessageFactory {

    /**
     * Session ID
     */

    public int sessionID;

    /**
     * Socket Handler
     */

    public static socketHandler sh = socketHandler.getInstance();


    /**
     * Map of session ID to DatagramPacket
     */

    public Map<DatagramPacket, Integer> sessionMap = new HashMap<>();

    /**
     * Creates a message for commands with no addresses
     *
     * @param command command to create message for
     * @return DatagramPacket
     * @throws IOException if error occurs
     */

    public DatagramPacket createMessage(String command) throws IOException {
        int sessionID = generateSessionID();
        Message m = new Message(MessageType.getByCmd(command), ErrorType.getByCode(0), sessionID);

        DatagramPacket packet = new DatagramPacket(m.encode(), m.encode().length, sh.getInetAddress(), sh.getPort());

        sessionMap.put(packet, sessionID);

        return packet;
    }

    /**
     * Creates a message for commands with addresses
     *
     * @param command command to create message for
     * @param addressList list of addresses
     * @return DatagramPacket
     * @throws IOException if error occurs
     */

    public DatagramPacket createMessage(String command, List<InetSocketAddress> addressList) throws IOException {
        int sessionID = generateSessionID();
        Message m = new Message(MessageType.getByCmd(command), ErrorType.getByCode(0), sessionID);

        List<InetSocketAddress> addresses = Objects.requireNonNull(addressList, "Address list cannot be null");
        for (InetSocketAddress address : addresses) {
            m.addAddress(address);
        }

        DatagramPacket packet = new DatagramPacket(m.encode(), m.encode().length, sh.getInetAddress(), sh.getPort());

        sessionMap.put(packet, sessionID);

        return packet;
    }

    /**
     * Generates a session ID
     *
     * @return session ID
     */


    private int generateSessionID() {
        Random rand = new Random();
        sessionID = rand.nextInt(256);
        return sessionID;
    }

    /**
     * Gets the session map
     *
     * @return session map
     */

    public Map<DatagramPacket, Integer> getSessionMap() {
        return sessionMap;
    }

}
