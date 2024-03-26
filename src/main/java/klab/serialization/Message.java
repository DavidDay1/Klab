package klab.serialization;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a response message
 *
 * @version 1.0
 */
abstract public class Message {
    /**
     * Delimiter for message serialization
     */
    private static final byte DELIM = '\n';

    /**
     * Length of the message ID
     */
    private static final int ID_LENGTH = 15;


    /**
     * ID of the message
     */
    protected byte[] msgID;

    /**
     * Time to live of the message
     */
    protected int ttl;

    /**
     * Routing service of the message
     */
    protected RoutingService routingService;


    /**
     * Length of the message
     */
    protected int length;

    /**
     * Constructs base message with given values
     *
     * @param msgID message ID
     * @param ttl message ttl
     * @param routingService message routing service
     * @throws BadAttributeValueException if any parameter fails validation
     */

    Message(byte[] msgID, int ttl, RoutingService routingService) throws BadAttributeValueException {
        setID(msgID);
        setTTL(ttl);
        setRoutingService(routingService);
    }

    /**
     * Encode message to given output sink
     *
     * @param out output sink
     * @throws IOException if I/O problem or out is null
     */

    public void encode(MessageOutput out) throws IOException {
        if (out == null) {
            throw new IOException("MessageOutput is null");
        }
        out.write(getMessageType());
        out.write(msgID);
        out.write(ttl);
        out.write(routingService.getCode());
    }


    /**
     * Deserializes message from input source
     *
     * @param in deserialization input source
     * @return a specific message resulting from deserialization
     * @throws IOException                if in is null or I/O problem occurs
     * @throws BadAttributeValueException if any parsed value fails validation
     */

    public static Message decode(MessageInput in) throws IOException, BadAttributeValueException {

        if (in == null) {
            throw new IOException("MessageInput is null or too short");
        }

        int type = in.read();
        if (type == -1) {
            throw new IOException("MessageInput is null or too short");
        }
        byte[] id = new byte[ID_LENGTH];
        id = in.readBytes(ID_LENGTH);
        int ttl = in.read();
        int routingService = in.read();
        RoutingService rs = RoutingService.getRoutingService(routingService);
        int length = in.readUnsignedShort();

        Message m;
        switch (type) {
            case 1:
                m = Search.decode(in, id, ttl, rs, length);
                break;

            case 2:
                m = Response.decode(in, id, ttl, rs, length);
                break;
            default:
                throw new BadAttributeValueException("Invalid message type", "type");
        }
        return m;

    }



    /**
     * Get message type
     *
     * @return message type
     */


    abstract public int getMessageType();

    /**
     * Get ID
     *
     * @return ID of message
     */

    public byte[] getID() {
        return msgID;
    }

    /**
     * Set ID
     *
     * @param id new ID
     * @throws BadAttributeValueException if id is null or invalid length
     * @return this Message with new ID
     */

    public Message setID(byte[] id) throws BadAttributeValueException {
        if (id == null) {
            throw new BadAttributeValueException("id is null", "id");
        } else if (id.length != ID_LENGTH) {
            throw new BadAttributeValueException("id is not correct length", "id");
        } else {
            this.msgID = id;
            return this;
        }
    }

    /**
     * Get TTL
     *
     * @return TTL of message
     */

    public int getTTL() {
        return ttl;
    }

    /**
     * Set TTL
     *
     * @param ttl new TTL
     * @return this Message with new TTL
     * @throws BadAttributeValueException if ttl is invalid
     */

    public Message setTTL(int ttl) throws BadAttributeValueException {
        if (ttl < 0 || ttl > 255) {
            throw new BadAttributeValueException("ttl is invalid", "ttl");
        } else {
            this.ttl = ttl;
            return this;
        }
    }


    /**
     * Get routing service
     *
     * @return routing service
     */

    public RoutingService getRoutingService() {
        return routingService;
    }

    /**
     * Set routing service
     *
     * @param routingService new routing service
     * @return this Message with new routing service
     * @throws BadAttributeValueException if routingService is null
     */

    public Message setRoutingService(RoutingService routingService) throws BadAttributeValueException {
        if (routingService == null) {
            throw new BadAttributeValueException("routingService is null", "routingService");
        } else {
            this.routingService = routingService;
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message message)) return false;
        return ttl == message.ttl && Arrays.equals(msgID, message.msgID) && routingService == message.routingService;
    }

    /**
     * Get length
     *
     * @return length of message
     */

    public int getLength() {
        return length;
    }


    /**
     * Set length
     *
     * @param length new length
     */

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(ttl, routingService);
        result = 31 * result + Arrays.hashCode(msgID);
        return result;
    }
}
