package metanode.serialization;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * Represents a MetANode Message
 *
 * @version 1.0
 */

public class Message {
    private MessageType type;
    private ErrorType error;
    private int sessionID;
    private Set<InetSocketAddress> addressSet = new LinkedHashSet<>();
    private static final int VERSION = 0b0100;

    /**
     * Constructor new packet from attributes
     *
     * @param messageType type of message
     * @param errorType   error type (if any) of message
     * @param id          session ID of message
     * @throws IllegalArgumentException if bad attribute value given. Note that only an Answer Request may have
     *                                  non-zero error
     */

    public Message(MessageType messageType, ErrorType errorType, int id) throws IllegalArgumentException {
        if (messageType == null || errorType == null) {
            throw new IllegalArgumentException("Message type and error type cannot be null");
        }
        this.type = Objects.requireNonNull(messageType);
        this.error = Objects.requireNonNull(errorType);
        if (!Objects.equals(type.getCmd(), "AR") && error.getCode() != 0) {
            throw new IllegalArgumentException("Only AnswerRequest can have a non-zero error code");
        }
        this.setSessionID(id);
    }


    /**
     * Construct new packet from byte array
     *
     * @param buf buffer containing encoded packet
     * @throws IOException              if byte array too long/short or buf is null
     * @throws IllegalArgumentException if bad attribute value
     */

    public Message(byte[] buf) throws IOException, IllegalArgumentException {
        if (buf == null) {
            throw new IOException("Buffer cannot be null");
        }
        if (buf.length < 4) {
            throw new IOException("Buffer must be at least 4 bytes long");
        }
        ByteArrayInputStream in = new ByteArrayInputStream(buf);

        int temp = in.read();
        type = MessageType.getByCode(temp);

        if (type == null) {
            throw new IllegalArgumentException("Invalid message type: " + temp);
        }

        byte tempVer = (byte) (((byte) temp & 0xFFFFFFF0) >> 4);
        if (tempVer != VERSION) {
            throw new IllegalArgumentException("Version mismatch: " + tempVer + " vs " + VERSION);
        }

        error = ErrorType.getByCode(in.read());

        if (error == null) {
            throw new IllegalArgumentException("Invalid error code");
        }

        if (error.getCode() != 0 && !Objects.equals(type.getCmd(), "AR")) {
            throw new IllegalArgumentException("Only AnswerRequest can have a non-zero error code");
        }

        /*
        reading the session ID from the buffer
        sessionID is just some number 0-255
         */

        this.setSessionID(in.read() & 0xFF);

        /*
        how many address are in the payload
         */
        int count = in.read();

        if (count < 0) {
            throw new IllegalArgumentException("Address count must be positive");
        }

        if (count * 6 != in.available()) {
            throw new IOException("Buffer length does not match address count");
        }
        this.addressSet = new LinkedHashSet<>(count);

        for (int i = 0; i < count; i++) {
            byte[] ip = in.readNBytes(4);
            int port = (in.read() & 0xFF) << 8 | (in.read() & 0xFF);
            InetAddress addr = InetAddress.getByAddress(ip);
            this.addAddress(new InetSocketAddress(addr, port));
        }

    }


    /**
     * Return encoded message in byte array
     *
     * @return encoded message byte array
     * @throws IOException if error encoding packet
     */
    public byte[] encode() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write((VERSION << 4) | type.getCode());
        if (Objects.equals(type.getCmd(), "AR")) {
            out.write(error.getCode());
        } else {
            out.write(0);
        }
        out.write(sessionID);
        out.write(addressSet.size());
        for (InetSocketAddress addr : addressSet) {
            byte[] ip = addr.getAddress().getAddress();
            out.write(ip);
            out.write((addr.getPort() >> 8) & 0xFF);
            out.write(addr.getPort() & 0xFF);
        }
        return out.toByteArray();
    }

    /**
     * Returns a String representation
     *
     * @return string representation of message
     */

    @Override
    public String toString() {
        StringBuilder build = new StringBuilder("Type=" + type + " Error=" + error + " Session ID=" + sessionID
                + " Addrs=");
        for (InetSocketAddress addr : addressSet) {
            build.append(addr.getAddress().getHostAddress()).append(":").append(addr.getPort()).append(" ");
        }
        return build.toString();
    }

    /**
     * Get packet type
     *
     * @return packet type
     */

    public MessageType getType() {
        return type;
    }

    /**
     * Get error
     *
     * @return error
     */

    public ErrorType getError() {
        return error;
    }

    /**
     * Set session ID
     *
     * @param sessionID new session ID
     * @return this Message with new session ID
     * @throws IllegalArgumentException if session ID is invalid
     */
    public Message setSessionID(int sessionID) throws IllegalArgumentException {
        if ((sessionID & 0xFFFFFF00) != 0) {
            throw new IllegalArgumentException("Session ID must be 0-255 was:" + sessionID);
        }
        this.sessionID = sessionID;
        return this;
    }


    /**
     * Get session ID
     *
     * @return session ID
     */

    public int getSessionID() {
        return sessionID;
    }

    /**
     * Get non-null list of addresses
     *
     * @return list of addresses
     */

    public List<InetSocketAddress> getAddrList() {
        return addressSet.stream().toList();
    }

    /**
     * Add new address
     *
     * @param newAddress new address to add. If the list of addresses already contains the address,
     *                   the list remains unchanged.
     * @return this Message with additional address
     * @throws IllegalArgumentException if newAddress is null, this type of MetaNode message does not have addresses,
     *                                  or if too manny addresses
     */

    public Message addAddress(InetSocketAddress newAddress) throws IllegalArgumentException {
        if (type.getCmd().equals("RN") || type.getCmd().equals("RM") || newAddress == null || addressSet.size() == 255) {
            throw new IllegalArgumentException("Cannot add an address to this message");
        }
        if (newAddress.getAddress() instanceof Inet4Address) {
            addressSet.add(newAddress);
        } else {
            throw new IllegalArgumentException("Only IPv4 addresses are supported");
        }
        return this;
    }


    /**
     * equals method
     *
     * @param o object to compare to
     * @return true if equal, false otherwise
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message message = (Message) o;
        return sessionID == message.sessionID && type == message.type && error == message.error &&
                Objects.equals(this.getAddrList(), ((Message) o).getAddrList());
    }

    /**
     * hashCode method
     *
     * @return hash code of object
     */

    @Override
    public int hashCode() {
        return Objects.hash(type, error, sessionID, getAddrList());
    }
}
