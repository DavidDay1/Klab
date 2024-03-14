package klab.serialization.test;

import klab.serialization.Message;
import klab.serialization.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.*;

public class ResponseTest {
        public static byte[] goodID = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
                        0x07, 0x08, 0x09, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15 };

        Response r;

        @BeforeEach
        public void setUp() throws BadAttributeValueException {
                r = new Response(goodID, 3, RoutingService.DEPTHFIRST,
                                new InetSocketAddress("192.168.0.0", 8000));
        }

        @ParameterizedTest
        @ArgumentsSource(ConstructorOkParamsProvider.class)
        void testConstructorOkParams(byte[] msgID, int ttl, RoutingService rs,
                        InetSocketAddress rh) throws BadAttributeValueException {
                Message m = new Response(msgID, ttl, rs, rh);
        }

        static class ConstructorOkParamsProvider implements ArgumentsProvider {

                @Override
                public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
                        return Stream.of(
                                        Arguments.of(goodID, 3, RoutingService.DEPTHFIRST,
                                                        new InetSocketAddress("192.0.0.0", 8000)),
                                        Arguments.of(goodID, 3, RoutingService.DEPTHFIRST,
                                                        new InetSocketAddress("191.255.0.0", 4000)),
                                        Arguments.of(goodID, 3, RoutingService.DEPTHFIRST,
                                                        new InetSocketAddress("1.0.0.0", 0)),
                                        Arguments.of(goodID, 3, RoutingService.DEPTHFIRST,
                                                        new InetSocketAddress("255.255.255.255", 8000))

                        );
                }
        }

        @ParameterizedTest
        @ArgumentsSource(ConstructorBadParamsProvider.class)
        void testConstructorBadParams(byte[] msgID, int ttl, RoutingService rs,
                        InetSocketAddress rh) throws BadAttributeValueException {
                assertThrows(BadAttributeValueException.class,
                                () -> new Response(msgID, ttl, rs, rh));
        }

        static class ConstructorBadParamsProvider implements ArgumentsProvider {

                public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
                        return Stream.of(
                                        Arguments.of(goodID, 3, RoutingService.DEPTHFIRST,
                                                        new InetSocketAddress("224.0.0.0", 8000)),
                                        Arguments.of(goodID, 3, RoutingService.DEPTHFIRST, null),
                                        Arguments.of(goodID, 3, RoutingService.DEPTHFIRST,
                                                        new InetSocketAddress("fe80::5e00:eb60:357f:9678", 8000))

                        );
                }
        }

        @Test
        public void testToString() throws BadAttributeValueException {
                Response r = new Response(goodID, 3, RoutingService.DEPTHFIRST,
                                new InetSocketAddress("1.2.3.4", 5678));
                r.addResult(new Result(new byte[] { 0x01, 0x02, (byte) 0x03, (byte) 0x04 },
                                500, "readme.txt"));
                r.addResult(new Result(new byte[] { 0x10, 0x20, (byte) 0x30, (byte) 0x40 },
                                105, "install.me"));
                assertEquals(r.toString(),
                                "Response: ID=010203040506070809101112131415 TTL=3 Routing=DEPTHFIRST Host=1.2.3" +
                                                ".4:5678 [Result: FileID=01020304 FileSize=500 bytes FileName=readme.txt, Result: FileID=10203040 "
                                                +
                                                "FileSize=105 bytes FileName=install.me]");
        }

        @Test
        public void testGetResponseHost() {
                InetSocketAddress addr = new InetSocketAddress("192.168.0.0", 8000);
                assertEquals(r.getResponseHost(), addr);
        }

        @Test
        public void testGetResultList() throws BadAttributeValueException {
                List<Result> rList = new ArrayList<>();
                rList.add(new Result(new byte[] { 0x01, 0x02, (byte) 0x03, (byte) 0x04 },
                                500, "readme.txt"));
                rList.add(new Result(new byte[] { 0x10, 0x20, (byte) 0x30, (byte) 0x40 },
                                105, "install.me"));

                r.addResult(new Result(new byte[] { 0x01, 0x02, (byte) 0x03, (byte) 0x04 },
                                500, "readme.txt"));
                r.addResult(new Result(new byte[] { 0x10, 0x20, (byte) 0x30, (byte) 0x40 },
                                105, "install.me"));

                assertEquals(r.getResultList(), rList);
        }

        @Test
        public void testAddNullResult() {
                assertThrows(BadAttributeValueException.class, () -> r.addResult(null));
        }

        @Test
        public void testAddResultTooMany() throws BadAttributeValueException {
                for (int i = 0; i < 255; i++) {
                        r.addResult(new Result(new byte[] { 0x05, 0x13, (byte) 0xA1, (byte) 0xCD },
                                        500, "readme.txt"));
                }
                assertThrows(BadAttributeValueException.class,
                                () -> r.addResult(new Result(new byte[] { 0x05, 0x13, (byte) 0xA1, (byte) 0xCD },
                                                500, "readme.txt")));
        }

        @Test
        void testDecodeOkParams() throws BadAttributeValueException, IOException {
                byte[] msg = new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 31, 2, (byte) 0xff,
                                (byte) 0xff, (byte) 192, 0,
                                0, 0, 1, 2, 3, 4, 0, 0, 0, 30, 'f', 'o', 'o', '\n', 5, 6, 7,
                                8, 0, 0, 0, 10, 'o', 'o', 'f', '\n' };
                Response r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(msg)));
                assertAll(() -> assertArrayEquals(r.getID(), new byte[] { 0, 0, 0, 0, 0, 0, 0,
                                0, 0, 0, 0, 0, 0, 0, 0 }),
                                () -> assertEquals(r.getMessageType(), 2),
                                () -> assertEquals(r.getTTL(), 3),
                                () -> assertEquals(r.getRoutingService(),
                                                RoutingService.BREADTHFIRST),
                                () -> assertEquals(r.getResponseHost(), new InetSocketAddress(
                                                "192.0.0.0", 65535)),
                                () -> assertArrayEquals(new byte[] { 1, 2, 3, 4 },
                                                r.getResultList().get(0).getFileID()),
                                () -> assertEquals(30L, r.getResultList().get(0).getFileSize()),
                                () -> assertEquals("foo",
                                                r.getResultList().get(0).getFileName()),
                                () -> assertArrayEquals(new byte[] { 5, 6, 7, 8 },
                                                r.getResultList().get(1).getFileID()),
                                () -> assertEquals(10L, r.getResultList().get(1).getFileSize()),
                                () -> assertEquals("oof",
                                                r.getResultList().get(1).getFileName()));
        }

        @ParameterizedTest
        @ArgumentsSource(DecodeBadParamsProvider.class)
        void testDecodeBadParams(byte[] msg) {
                assertThrows(BadAttributeValueException.class,
                                () -> Message.decode(new MessageInput(new ByteArrayInputStream(msg))));
        }

        static class DecodeBadParamsProvider implements ArgumentsProvider {

                public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
                        return Stream.of(
                                        Arguments.of((Object) new byte[] { 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                                        0, 3, 0, 0, 31, 2, (byte) 0xff, (byte) 0xff, (byte) 192,
                                                        (byte) 168,
                                                        0, 0, 1, 2, 3, 4, 0, 0, 0, 30, 'f', 'o', 'o',
                                                        '\n', 5, 6, 7, 8, 0, 0, 0, 10, 'o', 'o', 'f', '\n' }),
                                        Arguments.of((Object) new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                                        0, 3, 5, 0, 31, 2,
                                                        (byte) 0xff, (byte) 0xff, (byte) 192, (byte) 168,
                                                        0, 0, 1, 2, 3, 4, 0, 0, 0, 30, 'f', 'o', 'o',
                                                        '\n', 5, 6, 7, 8, 0, 0, 0, 10, 'o', 'o', 'f', '\n' }),
                                        Arguments.of((Object) new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                                        0, 3, 1, 0, 31, 2,
                                                        (byte) 0xff, (byte) 0xff, (byte) 224, 0,
                                                        0, 0, 1, 2, 3, 4, 0, 0, 0, 30, 'f', 'o', 'o',
                                                        '\n', 5, 6, 7, 8, 0, 0, 0, 10, 'o', 'o', 'f', '\n' })

                        );
                }
        }

        @Test
        public void testEncode() throws NullPointerException, IOException,
                        BadAttributeValueException {
                byte[] msg = new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 31, 2, (byte) 0xff,
                                (byte) 0xff, (byte) 192, (byte) 168,
                                0, 0, 1, 2, 3, 4, 0, 0, 0, 30, 'f', 'o', 'o', '\n', 5, 6, 7,
                                8, 0, 0, 0, 10, 'o', 'o', 'f', '\n' };

                Response r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(msg)));
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                MessageOutput mo = new MessageOutput(bos);
                r.encode(mo);
                assertArrayEquals(msg, bos.toByteArray());
        }

        @Test
        public void testResponseEquals() throws BadAttributeValueException {
                Response r1 = new Response(goodID, 4, RoutingService.DEPTHFIRST,
                                new InetSocketAddress("192.168.0.0", 8000));
                Response r2 = new Response(goodID, 4, RoutingService.DEPTHFIRST,
                                new InetSocketAddress("192.168.0.0", 8000));
                assertTrue(r1.equals(r2));
        }

        @Test
        public void testResponseEqualsBad() throws BadAttributeValueException {
                Response r1 = new Response(goodID, 4, RoutingService.DEPTHFIRST,
                                new InetSocketAddress("192.168.0.0", 8000));
                r1.addResult(new Result(new byte[] { 0x05, 0x13, (byte) 0xA1, (byte) 0xCD },
                                500, "readme.txt"));
                r1.addResult(new Result(new byte[] { 0x12, 0x34, (byte) 0x56, (byte) 0x78 },
                                105, "install.me"));
                Response r2 = new Response(goodID, 4, RoutingService.DEPTHFIRST,
                                new InetSocketAddress("192.168.0.0", 8000));
                r2.addResult(new Result(new byte[] { 0x12, 0x34, (byte) 0x56, (byte) 0x78 },
                                105, "install.me"));
                r2.addResult(new Result(new byte[] { 0x05, 0x13, (byte) 0xA1, (byte) 0xCD },
                                500, "readme.txt"));
                assertFalse(r1.equals(r2));
        }

    @Test
    public void encodeValid() throws IOException, BadAttributeValueException {
            Response r = new Response(new byte[]{15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1}, 50, RoutingService.BREADTHFIRST,
                    new InetSocketAddress("2.2.2.2", 13));
            byte[] enc = new byte[]{2, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 50, 0, 0, 7, 0, 0, 13, 2, 2, 2, 2};
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            MessageOutput mo = new MessageOutput(bos);
            r.encode(mo);
            assertArrayEquals(enc, bos.toByteArray());

    }

//    @Test
//    public void prematureEos() throws IOException, BadAttributeValueException {
//        byte[] msg = new byte[]{2, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 50, 0, 0, 7, 0, 0, 13, 2, 2, 2, 2};
//        assertThrows(BadAttributeValueException.class,
//                ()->(Response) Message.decode(new MessageInput(new ByteArrayInputStream(msg))));
//    }
}
