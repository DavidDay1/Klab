package klab.serialization.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.stream.Stream;

import klab.serialization.BadAttributeValueException;
import klab.serialization.Message;
import klab.serialization.MessageInput;
import klab.serialization.RoutingService;
import klab.serialization.Search;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {
    @Test
    void testValidInputs() throws NullPointerException, IOException, BadAttributeValueException {
        byte[] enc = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 3, 'f', 'o', 'o' };
        Search r = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
        assertAll(() -> assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, r.getID()),
                () -> assertEquals(3, r.getTTL()),
                () -> assertEquals(RoutingService.BREADTHFIRST, r.getRoutingService()),
                () -> assertEquals("foo", r.getSearchString()));
    }

    public static byte[] goodID = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
            0x07, 0x08, 0x09, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15 };
    public Message m;

    @BeforeEach
    public void setUp() throws BadAttributeValueException {
        m = new Search(goodID, 4, RoutingService.DEPTHFIRST, "foo");
    }

    @ParameterizedTest
    @ArgumentsSource(ConstructorValidParamsProvider.class)
    void testConstructorValidParams(byte[] msgID, int ttl, RoutingService rs,
            String search) throws BadAttributeValueException {
        Message m = new Search(msgID, ttl, rs, search);
    }

    static class ConstructorValidParamsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(goodID, 5, RoutingService.DEPTHFIRST, "foo"),
                    Arguments.of(goodID, 0, RoutingService.BREADTHFIRST, "foo"),
                    Arguments.of(goodID, 255, RoutingService.BREADTHFIRST,
                            "foo"));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ConstructorBadParamsProvider.class)
    void testConstructorBadParams(byte[] msgID, int ttl, RoutingService rs,
            String search) {
        assertThrows(BadAttributeValueException.class,
                () -> new Search(msgID, ttl, rs, search));
    }

    static class ConstructorBadParamsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(new byte[] { 0x01, 0x02, 0x03 }, 4,
                            RoutingService.DEPTHFIRST, "foo"),
                    Arguments.of(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
                            0x07, 0x08, 0x09, 0x10, 0x11, 0x12, 0x13, 0x14,
                            0x15, 0x16 }, 4, RoutingService.BREADTHFIRST, "foo"),
                    Arguments.of(null, 4,
                            RoutingService.DEPTHFIRST, "foo"),
                    Arguments.of(goodID, -4,
                            RoutingService.DEPTHFIRST, "foo"),
                    Arguments.of(goodID, 256,
                            RoutingService.DEPTHFIRST, "foo"),
                    Arguments.of(goodID, 4, null, "foo"));
        }
    }

    @Test
    public void testGetType() throws BadAttributeValueException {
        assertEquals(m.getMessageType(), 1);
    }

    @Test
    public void testGetID() throws BadAttributeValueException {
        assertArrayEquals(m.getID(), goodID);
    }

    @Test
    public void testGetTTL() throws BadAttributeValueException {
        assertEquals(m.getTTL(), 4);
    }

    @Test
    public void testGetRoutingService() throws BadAttributeValueException {
        assertEquals(m.getRoutingService(), RoutingService.DEPTHFIRST);
    }

    @ParameterizedTest
    @ArgumentsSource(SetIDBadParamsProvider.class)
    void testSetIDBadValues(byte[] msgID) {
        assertThrows(BadAttributeValueException.class,
                () -> m.setID(msgID));
    }

    static class SetIDBadParamsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of((Object) new byte[] { 0x01, 0x02, 0x03 }),
                    Arguments.of((Object) new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
                            0x07, 0x08, 0x09, 0x10, 0x11, 0x12, 0x13, 0x14,
                            0x15, 0x16 }),
                    Arguments.of((Object) null));
        }
    }

    @Test
    void testSetIDGoodValue() {
        assertDoesNotThrow(() -> m.setID(goodID));
        assertEquals(m.getID(), goodID);
    }

    @ParameterizedTest
    @ValueSource(ints = { -4, 256 })
    public void testSetTTLBadValue(int ttl) {
        assertThrows(BadAttributeValueException.class, () -> m.setTTL(ttl));
    }

    @ParameterizedTest
    @ValueSource(ints = { 4, 255, 0 })
    void testSetTTLGoodValue(int ttl) {
        assertDoesNotThrow(() -> m.setTTL(ttl));
        assertEquals(m.getTTL(), ttl);
    }

    @Test
    void testSetRoutingServiceNull() {
        assertThrows(BadAttributeValueException.class,
                () -> m.setRoutingService(null));
    }

    @Test
    void testSetRoutingServiceGoodValue() {
        assertDoesNotThrow(() -> m.setRoutingService(RoutingService.BREADTHFIRST));
        assertEquals(m.getRoutingService(), RoutingService.BREADTHFIRST);
    }

    @Test
    void testDoubleEOF() throws IOException, BadAttributeValueException {
        byte[] enc = {1, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 50, 0, 0, 2, 110, 101};
        MessageInput in = new MessageInput(new ByteArrayInputStream(enc));

        Search r = (Search) Message.decode(in);
        assertEquals("ne", r.getSearchString());
    }


}
