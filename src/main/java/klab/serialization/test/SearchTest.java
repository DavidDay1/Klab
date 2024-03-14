package klab.serialization.test;

import klab.serialization.Message;
import klab.serialization.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SearchTest {
    public static byte[] goodID = new byte[]{0x01,0x02,0x03,0x04,0x05,0x06,
            0x07,0x08,0x09,0x10,0x11,0x12,0x13,0x14, 0x15};

    public Search s;

    @BeforeEach
    public void setUp() throws BadAttributeValueException {
        s = new Search(goodID, 4, RoutingService.DEPTHFIRST, "Lipa");
    }

    @ParameterizedTest
    @ArgumentsSource(ConstructorOkParamsProvider.class)
    void testConstructorOkParams(byte[] msgID, int ttl, RoutingService rs,
                                 String search) throws BadAttributeValueException {
        Message m = new Search(msgID, ttl, rs, search);
    }


    static class ConstructorOkParamsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(goodID, 255, RoutingService.BREADTHFIRST,
                            "foo"),
                    Arguments.of(goodID, 4, RoutingService.DEPTHFIRST, ".txt" +
                            ".123"),
                    Arguments.of(goodID, 0, RoutingService.BREADTHFIRST,
                            "under_score.hyphen-"),
                    Arguments.of(goodID, 255, RoutingService.BREADTHFIRST,
                            "CAPITALLETTERSWOOHOOOOOOOOandlowercaseandnumbers123434526"),
                    Arguments.of(goodID, 16, RoutingService.BREADTHFIRST, "")
            );
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ConstructorBadParamsProvider.class)
    void testConstructorBadParams(byte[] msgID, int ttl, RoutingService rs,
                                  String search) throws BadAttributeValueException {
        assertThrows(BadAttributeValueException.class, ()-> new Search(msgID,
                ttl, rs, search));
    }


    static class ConstructorBadParamsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(goodID, 255, RoutingService.BREADTHFIRST,
                            "a space"),
                    Arguments.of(goodID, 4, RoutingService.DEPTHFIRST, "!@#$" +
                            "#%567"),
                    Arguments.of(goodID, 255, RoutingService.BREADTHFIRST,
                            null),
                    //string length longer than what payload length can hold
                    Arguments.of(goodID, 255, RoutingService.BREADTHFIRST,
                            Stream.generate(()-> "x").limit(65536).collect(joining()))
            );
        }
    }

    @Test
    public void testToString() throws BadAttributeValueException {
        Search s = new Search(goodID, 5, RoutingService.DEPTHFIRST, "Dog");
        assertEquals(s.toString(), "Search: ID=010203040506070809101112131415 TTL=5 Routing=DEPTHFIRST Search=Dog");
    }

    @Test
    public void testGetSearchString() throws BadAttributeValueException {
        assertEquals(s.getSearchString(), "Lipa");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"s p a c e", "!@#$%"})
    public void testSetSearchStringBadValue(String search){
        assertThrows(BadAttributeValueException.class,
                ()->s.setSearchString(search));
    }

    @Test
    void testSetSearchGoodValue(){
        assertDoesNotThrow(()->s.setSearchString("dog-man_123"));
        assertEquals(s.getSearchString(), "dog-man_123");
    }

    @Test
    public void testDecodeOk() throws NullPointerException, IOException,
            BadAttributeValueException {
        byte[] enc = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 3, 'f', 'o', 'o' };
        Search r = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
        assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0
                , 0, 0, 0, 0, 0, 0 }, r.getID());
        assertEquals(3, r.getTTL());
        assertEquals(RoutingService.BREADTHFIRST, r.getRoutingService());
        assertEquals("foo", r.getSearchString());
    }

    @Test
    public void testDecodeBadType(){
        byte[] enc = new byte[] { 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                , 0, 3, 0, 0, 3, 'f', 'o', 'o' };
        assertThrows(BadAttributeValueException.class,
                ()->Message.decode(new MessageInput(new ByteArrayInputStream(enc))));
    }

    @ParameterizedTest
    @ArgumentsSource(TooShortParamsProvider.class)
    void testMessageTooShort(byte[] msg) throws BadAttributeValueException {
        assertThrows(IOException.class,
                ()->Message.decode(new MessageInput(new ByteArrayInputStream(msg))));
    }


    static class TooShortParamsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of((Object) new byte[] { 1, 0, 0, 0, 0, 0, 0, 0
                            , 0, 0, 0, 0, 0, 0}),
                    Arguments.of((Object) new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                            , 0, 0, 0, 0, 3, 0, 0, 1}),
                    Arguments.of((Object) new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                            , 0, 0, 0, 0, 3, 0, 0, 20, 'b'}),
                    Arguments.of((Object) new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                            , 0, 0, 0, 0}),
                    Arguments.of((Object) new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                            , 0, 0, 0, 0, 1}),
                    Arguments.of((Object) new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                            , 0, 0, 0, 0, 3, 0})
            );
        }
    }

    @Test
    public void testDecodeNoPayload() throws BadAttributeValueException, IOException {
        byte[] enc = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                , 0, 3, 0, 0, 0 };
        Search r = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
        assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0
                , 0, 0, 0, 0, 0, 0 }, r.getID());
        assertEquals(3, r.getTTL());
        assertEquals(RoutingService.BREADTHFIRST, r.getRoutingService());
        assertEquals("", r.getSearchString());
    }

    @ParameterizedTest
    @ArgumentsSource(DecodeBadParamsProvider.class)
    void testDecodeBadParams(byte[] msg) {
        assertThrows(BadAttributeValueException.class,
                ()-> Message.decode(new MessageInput(new ByteArrayInputStream(msg))));
    }


    static class DecodeBadParamsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(new byte[] { 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                            , 0, 0, 0, 0, 3, 0, 0, 3, 'f', 'o', 'o'}),
                    Arguments.of(new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                            , 0, 0, 0, 0, 3, 2, 0, 3, 'f', 'o', 'o'})
            );
        }
    }

    @Test
    public void testEncode() throws NullPointerException, IOException,
            BadAttributeValueException {
        byte[] enc = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 3, 'f', 'o', 'o' };

        Search r = new Search(new byte[]{ 0, 0, 0, 0, 0, 0, 0, 0, 0
                , 0, 0, 0, 0, 0, 0 }, 3, RoutingService.BREADTHFIRST, "foo");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MessageOutput mo = new MessageOutput(bos);
        r.encode(mo);
        assertArrayEquals(enc, bos.toByteArray());
    }

    @Test
    public void testSearchEquals() throws BadAttributeValueException {
        Search s1 = new Search(goodID, 4, RoutingService.DEPTHFIRST, "dog");
        Search s2 = new Search(goodID, 4, RoutingService.DEPTHFIRST, "dog");
        assertTrue(s1.equals(s2));
    }

    @Test
    public void testSearchNotEquals() throws BadAttributeValueException {
        Search s1 = new Search(goodID, 4, RoutingService.DEPTHFIRST, "dog");
        Search s2 = new Search(goodID, 4, RoutingService.DEPTHFIRST, "cat");
        assertFalse(s1.equals(s2));
    }

    @Test
    public void encodeValid() throws IOException, BadAttributeValueException {
        byte[] enc= new byte[]{1, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 50, 0, 0, 2, 110, 101};
        Search s = new Search(new byte[]{15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1}, 50, RoutingService.DEPTHFIRST, "ne");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MessageOutput mo = new MessageOutput(bos);
        s.encode(mo);
        assertEquals(enc.length, bos.toByteArray().length);
    }

    @Test
    public void decodeValid() throws IOException, BadAttributeValueException {
        byte[] enc= new byte[]{1, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 50, 0, 0, 2, 110, 101};
        Search s = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
        assertEquals(50, s.getTTL());
        assertEquals(RoutingService.BREADTHFIRST, s.getRoutingService());
        assertEquals("ne", s.getSearchString());
    }

    @Test
    public void SinglePrematureEOS() throws IOException, BadAttributeValueException {
        byte[] enc= new byte[]{1, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 50, 0, -1, -1, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120};
        assertThrows(IOException.class, ()-> Message.decode(new MessageInput(new ByteArrayInputStream(enc))));

    }

    @Test
    public void DoublePrematureEOS() throws IOException, BadAttributeValueException {
        byte[] msg = new byte[]{1, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 50, 0, 0, 2, 110, 101};
        Search s = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(msg)));
        assertArrayEquals(new byte[] { 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 }, s.getID());
        assertEquals(50, s.getTTL());
        assertEquals(RoutingService.BREADTHFIRST, s.getRoutingService());
        assertEquals("ne", s.getSearchString());
    }

}
