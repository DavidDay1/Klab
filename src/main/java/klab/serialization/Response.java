package klab.serialization;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents a response message
 *
 * @version 1.0
 */

public class Response extends Message {
    /**
     * Type of message
     */
    int type;

    /**
     * Address and port of responding host
     */
    private InetSocketAddress responseHost;

    /**
     * Number of matches
     */
    protected int matches = 0;

    /**
     * List of results
     */
    private List<Result> resultList;

    /**
     * Constructs Response from given attributes
     *
     * @param msgID          message ID
     * @param ttl            message ttl
     * @param routingService message routing service
     * @param responseHost   Address and port of responding host
     * @throws BadAttributeValueException if bad or null attribute value
     */

    public Response(byte[] msgID, int ttl, RoutingService routingService, InetSocketAddress responseHost) throws BadAttributeValueException {
        super(msgID, ttl, routingService);
        setResponseHost(responseHost);
        this.resultList = new ArrayList<>(matches);
        this.type = 2;
        this.length = 7;
    }

    /**
     * Constructs Response from given attributes
     *
     * @param out output sink to write to
     * @throws IOException if bad or null attribute value
     */

    @Override
    public void encode(MessageOutput out) throws IOException {
        if (out == null) {
            throw new IOException("MessageOutput is null");
        }
        super.encode(out);
        out.writeShort(length);
        out.write(resultList.size());
        out.writeShort(responseHost.getPort());
        out.write4Bytes(responseHost.getAddress().getAddress());
        for (Result result : resultList) {
            result.encode(out);
        }
    }

    /**
     * Decode a response message from given input source
     *
     * @param in    input source to read from
     * @param msgID message ID
     * @param ttl   message ttl
     * @param rs    message routing service
     * @param length length of message
     * @return new response message
     * @throws BadAttributeValueException if bad or null attribute value
     * @throws IOException                if I/O problem or in is null
     */

    public static Response decode(MessageInput in, byte[] msgID, int ttl, RoutingService rs, int length) throws BadAttributeValueException, IOException {
        if (in == null) {
            throw new BadAttributeValueException("MessageInput is null", "in");
        }

        int matches = in.read();

        if (length < 7){
            throw new BadAttributeValueException("Length is invalid", "length");
        }

        if (matches > 0) {
            int matchesLength = matches * 8;
            int responseLength = length - 7;
            if (length < 0 || responseLength / matchesLength < 1) {
                throw new BadAttributeValueException("Length is invalid", "length");
            }
        }

        int ports = in.readUnsignedShort();
        byte[] address = in.readBytes(4);

        InetSocketAddress responseHost = new InetSocketAddress(InetAddress.getByAddress(address), ports);
        Response r = new Response(msgID, ttl, rs, responseHost);
        r.setMatches(matches);
        for (int i = 0; i < r.getMatches(); i++) {
            r.addResult(new Result(in));
        }
        if (length != r.length) {
            throw new BadAttributeValueException("Response length does not match", "length");
        }
        return r;
    }

    /**
     * Get message type
     *
     * @return message type
     */

    public int getMessageType() {
        this.type = 2;
        return type;
    }

    /**
     * Set number of matches
     *
     * @param found number of matches
     */

    private void setMatches(int found) {
        this.matches = found;
    }

    /**
     * Get number of matches
     *
     * @return number of matches
     */

    private int getMatches() {
        return matches;
    }


    /**
     * Returns a String representation
     *
     * @return String representation
     */

    @Override
    public String toString() {
        StringBuilder responseString = new StringBuilder("Response: ID=");
        for (int i = 0; i < this.msgID.length; i++) {
            responseString.append(String.format("%02X", this.msgID[i]));
        }
        responseString.append(" TTL=" + this.ttl + " Routing=" + this.routingService + " " +
                "Host=" + responseHost.getAddress().getHostAddress() + ':' + responseHost.getPort() + " [");
        for (Result result : resultList) {
            responseString.append(result.toString());
            if (resultList.indexOf(result) != resultList.size() - 1) {
                responseString.append(", ");
            }
        }
        responseString.append("]");
        return responseString.toString();
    }

    /**
     * Get address and port of responding host
     *
     * @return responding host address and port
     */

    public InetSocketAddress getResponseHost() {
        return responseHost;
    }

    /**
     * Set address and port of responding host
     *
     * @param responseHost responding host address and port
     * @return this Response with new response host
     * @throws BadAttributeValueException if responseHost is null or if the address is 1) multicast or 2) not IPv4
     *                                    address
     */

    public Response setResponseHost(InetSocketAddress responseHost) throws BadAttributeValueException {
        if (responseHost == null || responseHost.getAddress().isMulticastAddress() || !(responseHost.getAddress() instanceof Inet4Address)) {
            throw new BadAttributeValueException("responseHost ", "responseHost is not a valid unicast address");
        } else {
            this.responseHost = responseHost;
            return this;
        }
    }


    /**
     * Get list of results
     *
     * @return result list
     */

    public List<Result> getResultList() {
        if (resultList == null) {
            return null;
        } else {
            return resultList;
        }
    }

    /**
     * add result to list
     *
     * @param result new result to add to result list
     * @return this Response with new result added
     * @throws BadAttributeValueException if result is null or would make result list too long to encode
     */

    //check if encoding is too long
    public Response addResult(Result result) throws BadAttributeValueException {
        if (result == null || this.resultList.size() == 255) {
            throw new BadAttributeValueException("result is null", "result");
        } else {
            this.resultList.add(result);
            this.length += result.getSize();
            return this;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Response response)) return false;
        return ttl == response.ttl && Arrays.equals(msgID, response.msgID)
                && routingService == response.routingService
                && length == response.length
                && responseHost.equals(response.responseHost)
                && resultList.size() == response.resultList.size()
                && resultList.equals(response.resultList);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(ttl, routingService, responseHost, length, resultList);
        result = 31 * result + Arrays.hashCode(msgID);
        return result;
    }
}
