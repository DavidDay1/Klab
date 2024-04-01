package metanode.serialization;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

public class Message {
    private MessageType type;
    private ErrorType error;
    private int sessionID;
    private List<InetSocketAddress> addressList;

    public Message(MessageType messageType, ErrorType errorType, int id) throws IllegalArgumentException{
        this.type = messageType;
        this.error = errorType;
        this.sessionID = id;
    }

    public Message(byte[] buf) throws IOException, IllegalArgumentException {
        //TODO: read from buffer and set fields
    }


    public byte[] encode() {
        return null;
        //TODO: umm yea
    }

    public String toString() {
        StringBuilder build = new StringBuilder("Type=" + type + " Error=" + error + " Session ID" + sessionID + " Addrs=");
        for (InetSocketAddress addr : addressList) {
            build.append(addr.getAddress()).append(":").append(addr.getPort()).append(" ");
        }
        return build.toString();
    }



    public MessageType getType() {
        return type;
    }

    public ErrorType getError() {
        return error;
    }

    public Message setSessionID(int sessionID) throws IllegalArgumentException {
        if (sessionID < 0) {
            throw new IllegalArgumentException("Session ID must be non-negative");
        }
        this.sessionID = sessionID;
        return this;
    }

    public int getSessionID() {
        return sessionID;
    }

    public List<InetSocketAddress> getAddrList() {
        return addressList;
    }

    public Message addAddress(InetSocketAddress addr) throws IllegalArgumentException{
        if (addr == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        addressList.add(addr);
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message message)) return false;
        return sessionID == message.sessionID && type == message.type && error == message.error && Objects.equals(addressList, message.addressList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, error, sessionID, addressList);
    }
}
