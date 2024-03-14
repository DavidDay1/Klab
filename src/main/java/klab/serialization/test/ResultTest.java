package klab.serialization.test;

import klab.serialization.BadAttributeValueException;
import klab.serialization.MessageInput;
import klab.serialization.MessageOutput;
import klab.serialization.Result;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Result class
 *
 * @version 1.0
 */

public class ResultTest {
    private Result r;

    /**
     * Initializes a Result object with a fileID of 0x00000001, a fileSize of 1, and a fileName of "filename"
     */

    @BeforeEach
    public void init() {
        byte[] fileID = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01};
        long fileSize = 1L;
        String fileName = "filename";
        try {
            r = new Result(fileID, fileSize, fileName);
        } catch (BadAttributeValueException e) {
            e.printStackTrace();
        }
    }


    /**
     * Tests the Result class variable constructor
     */

    @Nested
    @DisplayName("Result Class Constructor Tests")
    class TestResultConstructor {

        /**
         * Tests the constructor with valid input
         * @param fileID - byte array of fileID
         * @param fileSize - long of fileSize
         * @param fileName - String of fileName
         */

        @ParameterizedTest
        @MethodSource("validByteIntStringProvider")
        void testValidConstructor(byte[] fileID, long fileSize, String fileName) {
            Assertions.assertDoesNotThrow(() -> {
                r = new Result(fileID, fileSize, fileName);
            });
            assertArrayEquals(r.getFileID(), fileID);
            assertEquals(r.getFileSize(), fileSize);
            assertEquals(r.getFileName(), fileName);
        }

        static Stream<Arguments> validByteIntStringProvider() {
            return Stream.of(
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, 5L, "filename"),
                    Arguments.arguments(new byte[]{127, 127, 0, 0}, 5L, "filename" ),
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, 4294967295L, "filename"),
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, 5L, "file_-name."),
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, 5L, "dfkajlsd")
            );
        }


        /**
         * Tests the constructor with invalid input
         * @param fileID - byte array of fileID
         * @param fileSize - long of fileSize
         * @param fileName - String of fileName
         */

        @ParameterizedTest
        @MethodSource("invalidByteIntStringProvider")
        void testInvalidConstructor(byte[] fileID, long fileSize, String fileName) {
            Assertions.assertThrows(BadAttributeValueException.class, () -> {
                r = new Result(fileID, fileSize, fileName);
            });
        }

        static Stream<Arguments> invalidByteIntStringProvider() {
            return Stream.of(
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, 5L, "s?ring"),
                    Arguments.arguments(new byte[]{1, 2}, 5L, "filename"),
                    Arguments.arguments(new byte[]{1, 2, 3, 4, 5, 6}, 5L, "filename"),
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, -5L, "filename"),
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, 9223372036854775807L, "filename"),
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, 5L, "!@#$"),
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, 5L, "s p a c e s"),
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, 5L, ""),
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, 5L, "\n"),
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, 5L, " "),
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, 5L, null)
            );
        }
    }

    /**
     * test the MessageInput Constructor for Result class
     */

    @Nested
    @DisplayName("Result Message Input Tests")
    class TestResultMessageInput {

        /**
         * Tests the MessageInput constructor with valid input
         * @throws NullPointerException - if MessageInput is null
         * @throws IOException - if MessageInput fails
         * @throws BadAttributeValueException - if MessageInput contains bad values
         */

        @Test
        void testValidMessageInput() throws NullPointerException, IOException, BadAttributeValueException {
            byte[] result = new byte[]{1, 2, 3, 4, 0, 0, 0, 5, 'f', 'o', 'o', '\n'};
            var r = new Result(new MessageInput(new ByteArrayInputStream(result)));
            assertArrayEquals(new byte[]{1, 2, 3, 4}, r.getFileID());
            assertEquals(5, r.getFileSize());
            assertEquals("foo", r.getFileName());
        }


        /**
         * Tests the MessageInput constructor with invalid input
         * @param fileID - byte array of fileID
         * @param fileSize - long of fileSize
         * @param fileName - String of fileName
         * @throws IOException - if MessageInput fails
         * @throws BadAttributeValueException - if MessageInput contains bad values
         */


        @ParameterizedTest
        @MethodSource("invalidByteLongStringProvider")
        void testInvalidMessageInput(byte[] fileID, long fileSize, String fileName) throws IOException, BadAttributeValueException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(100);
            MessageOutput out = new MessageOutput(bos);

            out.write4Bytes(fileID);
            out.writeUnsignedInt(fileSize);
            out.writeString(fileName);
            byte[] result = bos.toByteArray();

            assertThrows(BadAttributeValueException.class, () -> {
                r = new Result(new MessageInput(new ByteArrayInputStream(result)));
            });
        }

        static Stream<Arguments> invalidByteLongStringProvider() {
            return Stream.of(
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, 30L, "!"),
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, 30L, "s p a c e"),
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, 30L, ""),
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, 30L, "\n"),
                    Arguments.arguments(new byte[]{1, 2, 3, 4}, 30L, " ")
            );
        }


        /**
         * Tests the MessageInput constructor with multiple results in a single buffer
         * @throws IOException - if MessageInput fails
         * @throws BadAttributeValueException - if MessageInput contains bad values
         */


        @Test
        void testMultipleResults() throws IOException, BadAttributeValueException {
            byte[] result = new byte[] {1, 2, 3, 4, 0, 0, 0, 5, 'f', 'o', 'o', '\n', 5, 6, 7, 8, 0, 0, 0, 10, 'o', 'o', 'f', '\n'};
            MessageInput mi = new MessageInput(new ByteArrayInputStream(result));
            var r = new Result(mi);
            assertArrayEquals(new byte[] {1,2,3,4}, r.getFileID());
            assertEquals(5L, r.getFileSize());
            assertEquals("foo", r.getFileName());
            r = new Result(mi);
            assertArrayEquals(new byte[] {5,6,7,8}, r.getFileID());
            assertEquals(10L, r.getFileSize());
            assertEquals("oof", r.getFileName());
        }

        /**
         * Tests the MessageInput constructor with multiple results in a single buffer with one bad result
         * @throws IOException - if MessageInput fails
         * @throws BadAttributeValueException - if MessageInput contains bad values
         */

        @Test
        void testMultipleResultsInvalid() throws IOException, BadAttributeValueException {
            byte[] result = new byte[] {1, 2, 3, 4, 0, 0, 0, 5, 'f', 'o', 'o', '\n', 0, 0, 0, 10, 'o', 'o', 'f', '\n'};
            MessageInput mi = new MessageInput(new ByteArrayInputStream(result));
            r = new Result(mi);
            assertArrayEquals(new byte[] {1,2,3,4}, r.getFileID());
            assertEquals(5L, r.getFileSize());
            assertEquals("foo", r.getFileName());
            assertThrows(IOException.class, () -> {
                r = new Result(mi);
            });
        }

        /**
         * Test the MessageInput constructor with a small buffer
         * @throws IOException - if MessageInput fails
         */

        @Test
        void testInvalidMessageInputSmall() throws IOException {
            byte[] result = new byte[] {1, 2};
            MessageInput mi = new MessageInput(new ByteArrayInputStream(result));
            assertThrows(IOException.class, ()-> new Result(mi));
        }
    }

    /**
     * Tests the Result encode method
     */

    @Nested
    @DisplayName("Result Encode Tests")
    class TestEncode {

        /**
         * Tests the Result encode method with valid input
         * @throws BadAttributeValueException - if Result contains bad values
         * @throws IOException - if Result fails
         */

        @Test
        void testEncodeValidInput() throws BadAttributeValueException, IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            MessageOutput mo = new MessageOutput(bos);

            Assertions.assertDoesNotThrow(() -> r.encode(mo));
            Result r2 = new Result(new byte[]{1, 2, 3, 4}, 10L, "foo.txt");
            Assertions.assertDoesNotThrow(() -> r2.encode(mo));

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            MessageInput mi = new MessageInput(bis);
            Result r3 = new Result(mi);
            assertArrayEquals(r.getFileID(), r3.getFileID());
            assertEquals(r.getFileSize(), r3.getFileSize());
            assertEquals(r.getFileName(), r3.getFileName());
            r3 = new Result(mi);
            assertArrayEquals(r2.getFileID(), r3.getFileID());
            assertEquals(r2.getFileSize(), r3.getFileSize());
            assertEquals(r2.getFileName(), r3.getFileName());
        }

        /**
         * Tests the Result encode method with invalid input
         * @throws BadAttributeValueException - if Result contains bad values
         * @throws IOException - if Result fails
         */

        @Test
        void testEncodeNullOutput() {
            Assertions.assertThrows(IOException.class, () -> {
                r.encode(null);
            });
        }
    }

    /**
     * Tests the Result toString method
     * @throws BadAttributeValueException - if Result contains bad values
     */

    @Test
    void testToString() throws BadAttributeValueException, IOException {
        Result r = new Result(new byte[]{(byte) 0x11, (byte) 0x22, (byte) 0xAA, (byte) 0xBB}, 1000L, "filename.txt");
        assertEquals(r.toString(), "Result: FileID=1122AABB FileSize=1000 bytes FileName=filename.txt");
    }

    /**
     * Tests the Result getFileID method
     * @throws BadAttributeValueException - if Result contains bad values
     */

    @Test
    void testGetFileID() {
        byte[] fileID = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01};
        assertArrayEquals(r.getFileID(), fileID);
    }

    /**
     * Tests the Result setFileID method
     */

    @Test
    void testSetFileID() {
        byte[] fileID = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02};
        assertDoesNotThrow(() -> r.setFileID(fileID));
        assert (Arrays.equals(r.getFileID(), fileID));
    }

    /**
     * Tests the Result getFileSize method with invalid input
     */

    @Test
    void testGetFileSize() {
        Assertions.assertEquals(r.getFileSize(), 1L);
    }

    /**
     * Tests the Result setFileSize method
     */

    @Test
    void testSetFileSize() {
        assertDoesNotThrow(() -> r.setFileSize(2L));
        assertEquals(r.getFileSize(), 2L);
    }


    /**
     * Tests the Result setFileName method
     */

    @Test
    void testSetFileName() {
        assertDoesNotThrow(() -> r.setFileName("newfilename"));
        assertEquals(r.getFileName(), "newfilename");
    }


    /**
     * Tests the Result getFileName method
     */

    @Test
    void testGetFileName() {
        Assertions.assertEquals(r.getFileName(), "filename");
    }
}
