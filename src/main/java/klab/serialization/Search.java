package klab.serialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a search message
 * @version 1.0
 */

public class Search extends Message {
    private int type; //type of message
    private String searchString; //string to search for

    /**
     * Constructs a Search from given input source
     * @param msgID message ID
     * @param ttl message ttl
     * @param routingService message routing service
     * @param searchString string to search for
     * @throws BadAttributeValueException if any parsed value fails validation
     */

    public Search(byte[] msgID, int ttl, RoutingService routingService, String searchString) throws BadAttributeValueException{
        super(msgID, ttl, routingService);
        setSearchString(searchString);
        this.length = searchString.length();
        this.type = 1;
    }


    /**
     * Constructs a Search from given input source
     * @param out output sink
     * @throws IOException if out is null
     */

    @Override
    public void encode(MessageOutput out) throws IOException {
        if (out == null) {
            throw new IOException("MessageOutput is null");
        }
        super.encode(out);
        out.writeShort(length);
        out.writeLengthString(searchString, length);
    }

    /**
     * Decode a Search message from given input source
     * @param in input source
     * @param msgID message ID
     * @param ttl message ttl
     * @param rs routing service
     * @param length length of the search string
     * @return Search message
     * @throws BadAttributeValueException if any parsed value fails validation
     * @throws IOException if in is null
     */


    public static Search decode(MessageInput in, byte[] msgID, int ttl, RoutingService rs, int length) throws BadAttributeValueException, IOException {
        if (in == null) {
            throw new BadAttributeValueException("MessageInput is null", "in");
        }
        if (length < 0 || length > 65535) {
            throw new BadAttributeValueException("Length is invalid", "length");
        }
        String searchString = in.readLengthString(length);
        return new Search(msgID, ttl, rs, searchString);
    }


    /**
     * Returns a string representation of the search message
     * @return string representation of the search message
     */

    public String toString() {
        StringBuilder responseString = new StringBuilder("Search: ID=");
        for (int i = 0; i < this.msgID.length; i++) {
            responseString.append(String.format("%02X", this.msgID[i]));
        }
        responseString.append(" TTL=" + this.ttl + " Routing=" + this.routingService + " " +
                "Search=" + searchString);
        return responseString.toString();
    }

    /**
     * Returns the type of the message
     * @return type of the message
     */

    public int getMessageType() {
        this.type = 1;
        return type;
    }

    /**
     * Returns the search string
     * @return search string
     */

    public String getSearchString() {
        return searchString;
    }

    /**
     * Set the search string
     * @param searchString new search string
     * @throws BadAttributeValueException if searchString is null or too big
     * @return this Search with new search string
     */

    public Search setSearchString(String searchString) throws BadAttributeValueException {
        if (searchString == null || searchString.length() > 65535) {
            throw new BadAttributeValueException("searchString is null or too big", "searchString");
        }

        byte[] bytes = searchString.getBytes(StandardCharsets.US_ASCII);
        String asciiString = new String(bytes, StandardCharsets.US_ASCII);

        for (int i = 0; i < asciiString.length(); i++) {
            if (!Character.isLetterOrDigit(asciiString.charAt(i))) {
                if (asciiString.charAt(i) != '.' && asciiString.charAt(i) != '_' && searchString.charAt(i) != '-') {
                    throw new BadAttributeValueException("searchString contains non-ASCII characters", "searchString");
                }
            }
        }
        this.searchString = asciiString;
        this.length = searchString.length();
        return this;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Search search)) return false;
        return ttl == search.ttl && Arrays.equals(msgID, search.msgID) 
            && routingService == search.routingService
            && length == search.length
            && searchString.equals(search.searchString);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(ttl, routingService, searchString);
        result = 31 * result + Arrays.hashCode(msgID);
        return result;
    }
}
