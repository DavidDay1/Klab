package metanode.serialization.test;

import metanode.serialization.ErrorType;
import metanode.serialization.Message;
import metanode.serialization.MessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the Message class
 */

public class MessageTest {

    /**
     * Test that the version is 4
     */

    @Test
    public void badVersion() throws IOException {
        // Manually create a byte array that represents a Message object
        byte[] buf = new byte[]{0x50, 0, (byte) 0xFF, 0x00};

        // Create a new Message object using the byte array constructor

        Assertions.assertThrows(IllegalArgumentException.class, () -> new Message(buf));

    }

    /**
     * Test that message type can have a payload
     */

    @Test
    public void typeDataMisMatch() throws IOException {
        // Manually create a byte array that represents a Message object
        // Request Node does not have a data field
        byte[] buf = new byte[]{0x40, 0, (byte) 0xFF, 0x01, (byte) 192, (byte) 168, 1, 1, 0, 80};

        // Create a new Message object using the byte array constructor
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Message(buf));
    }

    /**
     * Test that message type can have a payload
     */

    @Test
    public void typeDataMisMatch2() throws IOException {
        // Manually create a byte array that represents a Message object
        // Request Node does not have a data field
        byte[] buf = new byte[]{0x41, 0, (byte) 0xFF, 0x01, (byte) 192, (byte) 168, 1, 1, 0, 80};

        // Create a new Message object using the byte array constructor
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Message(buf));
    }

    /**
     * Test that count is equal to number of addresses
     */

    @Test
    public void illegalCount() {
        // Manually create a byte array that represents a Message object
        byte[] buf = new byte[]{0x45, 0, (byte) 0xFF, 0x02, (byte) 192, (byte) 168, 1, 1, 0, 80};

        // Create a new Message object using the byte array constructor
        Assertions.assertThrows(IOException.class, () -> new Message(buf));
    }

    /**
     * Test that addresses are equal to count
     */

    @Test
    public void illegalData() throws IOException {
        byte[] buf = new byte[]{0x45, 0, (byte) 0XFF, 0x01, (byte) 192, (byte) 168, 1, 1, 0, 80, (byte) 127, 1, 1, 1, 0, 80};

        Assertions.assertThrows(IOException.class, () -> new Message(buf));
    }

    /**
     * Test types that can't have errors
     */

    @Test
    public void illegalError() {
        byte[] buf = new byte[]{0x45, 10, (byte) 0XFF, 0x02, (byte) 192, (byte) 168, 1, 1, 0, 80, (byte) 127, 1, 1, 1, 0, 80};

        Assertions.assertThrows(IllegalArgumentException.class, () -> new Message(buf));
    }


    /**
     * Test decode
     */

    @Test
    public void decode() throws IOException {
        // Manually create a byte array that represents a Message object
        byte[] buf = new byte[]{0x45, 0, (byte) 0xFF, 0x00};

        // Create a new Message object using the byte array constructor
        Message newMessage = new Message(buf);

        // Assert that the new Message object has the expected MessageType, ErrorType, and sessionID
        assertEquals(MessageType.NodeDeletions, newMessage.getType());
        assertEquals(ErrorType.None, newMessage.getError());
        assertEquals(0xFF, newMessage.getSessionID());
    }

    /**
     * Test decode with good address
     */

    @Test
    public void decodeWithGoodAddress() throws IOException {
        byte[] buf = new byte[]{0x45, 0, (byte) 0XFF, 0x01, (byte) 192, (byte) 168, 1, 1, 0, 80};

        Message newMessage = new Message(buf);

        assertEquals(MessageType.NodeDeletions, newMessage.getType());
        assertEquals(ErrorType.None, newMessage.getError());
        assertEquals(0xFF, newMessage.getSessionID());
        assertEquals(1, newMessage.getAddrList().size());
        assertEquals(new InetSocketAddress("192.168.1.1", 80), newMessage.getAddrList().get(0));

    }

    /**
     * Test decode with good many addresses
     */

    @Test
    public void decodeWithGoodManyAddresses() throws IOException {
        byte[] buf = new byte[]{0x45, 0, (byte) 0XFF, 0x02, (byte) 192, (byte) 168, 1, 1, 0, 80, (byte) 127, 1, 1, 1, 0, 80};

        Message newMessage = new Message(buf);

        assertEquals(MessageType.NodeDeletions, newMessage.getType());
        assertEquals(ErrorType.None, newMessage.getError());
        assertEquals(0xFF, newMessage.getSessionID());
        assertEquals(2, newMessage.getAddrList().size());
        assertEquals(new InetSocketAddress("192.168.1.1", 80), newMessage.getAddrList().get(0));
        assertEquals(new InetSocketAddress("127.1.1.1", 80), newMessage.getAddrList().get(1));

    }

    /**
     * Test decode with large port number
     */


    @Test
    public void decodeWithLargePortNum() throws IOException {
        byte[] buf = new byte[]{0x45, 0, (byte) 0XFF, 0x02, (byte) 192, (byte) 168, 1, 1, (byte) 255, (byte) 255, (byte) 127, 1, 1, 1, 0, 80};

        Message newMessage = new Message(buf);

        assertEquals(MessageType.NodeDeletions, newMessage.getType());
        assertEquals(ErrorType.None, newMessage.getError());
        assertEquals(0xFF, newMessage.getSessionID());
        assertEquals(2, newMessage.getAddrList().size());
        assertEquals(new InetSocketAddress("192.168.1.1", 0xFFFF), newMessage.getAddrList().get(0));
        assertEquals(new InetSocketAddress("127.1.1.1", 80), newMessage.getAddrList().get(1));

    }

    /**
     * Test decode with duplicate addresses
     */

    @Test
    public void decodeWithDuplicateAddresses() throws IOException {
        byte[] buf = new byte[]{0x45, 0, (byte) 0XFF, 0x02, (byte) 192, (byte) 168, 1, 1, 0, 80, (byte) 192, (byte) 168, 1, 1, 0, 80};

        Message newMessage = new Message(buf);

        assertEquals(1, newMessage.getAddrList().size());
        assertEquals(new InetSocketAddress("192.168.1.1", 80), newMessage.getAddrList().get(0));

    }

    /**
     * Test to string
     */

    @Test
    public void testToString() throws IOException {
        byte[] buf = new byte[]{0x45, 0, (byte) 0XFF, 0x02, (byte) 192, (byte) 168, 1, 1, 0, 80, (byte) 127, (byte) 1, 1, 1, 0, 80};

        Message newMessage = new Message(buf);

        assertEquals("Type=NodeDeletions Error=None Session ID=255 Addrs=192.168.1.1:80 127.1.1.1:80 ", newMessage.toString());

    }

    /**
     * Test to string with no addresses
     */

    @Test
    public void testToStringNoAddr() throws IOException {
        byte[] buf = new byte[]{0x45, 0, (byte) 0XFF, 0x00,};

        Message newMessage = new Message(buf);

        assertEquals("Type=NodeDeletions Error=None Session ID=255 Addrs=", newMessage.toString());

    }

    /**
     * Test encode
     */

    @Test
    public void testEncode() throws IOException {
        byte[] buf = new byte[]{0x45, 0, (byte) 0XFF, 0x02, (byte) 192, (byte) 168, 1, 1, 0, 80, (byte) 127, (byte) 0, 0, 1, 0, 80};

        Message newMessage = new Message(buf);
        byte[] encoded = newMessage.encode();
        assertTrue(Arrays.equals(buf, encoded));
    }


    @Test
    public void SpamEncode() throws IOException {
        byte[] buf = new byte[]{0x45, 0, (byte) 0XFF, 0x02, (byte) 192, (byte) 168, 1, 1, 0, 80, (byte) 127, (byte) 0, 0, 1, 0, 80};

        Message newMessage = new Message(buf);
        byte[] encoded = newMessage.encode();

        Message message2 = new Message(encoded);
        byte[] encoded2 = message2.encode();

        assertTrue(Arrays.equals(buf, encoded2));

    }

    /**
     * Test parameter constructor
     */

    @Test
    public void testElementConstructor() {
        MessageType type = MessageType.AnswerRequest;
        ErrorType error = ErrorType.System;
        int sessionID = 0xFF;

        Message newMessage = new Message(type, error, sessionID);

        assertEquals(type, newMessage.getType());
        assertEquals(error, newMessage.getError());
        assertEquals(0xFF, newMessage.getSessionID());
    }

    /**
     * Test parameter constructor illegal error value
     */

    @Test
    public void testElementConstructorBadError() {
        MessageType type = MessageType.NodeDeletions;
        ErrorType error = ErrorType.System;
        int sessionID = 0xFF;

        Assertions.assertThrows(IllegalArgumentException.class, () -> new Message(type, error, sessionID));
    }

    /**
     * Test parameter constructor with null type
     */

    @Test
    public void testNullElementConstructor() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> new Message(null, ErrorType.None, 0xFF));
    }

    /**
     * Test parameter constructor with null error
     */

    @Test
    public void testNullElementConstructor2() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> new Message(MessageType.RequestNodes, null, 0xFF));
    }

    /**
     * Donatest
     */

    @Test
    void test() throws IllegalArgumentException, IOException {
        Message msg = new Message(new byte[]{0x41, 0, 59, 0});
        assertEquals(MessageType.RequestMetaNodes, msg.getType());
        assertEquals(ErrorType.None, msg.getError());
        assertEquals(59, msg.getSessionID());
        assertEquals(0, msg.getAddrList().size());
    }

    @Test
    void encodeErrorType() throws IOException {
        byte[] buf = new byte[]{66, 20, -2, 1, 1, 1, 1, 1, 11, -72};
        Message m = new Message(buf);
        byte[] buf2 = m.encode();
        assertEquals(buf, buf2);

    }


}