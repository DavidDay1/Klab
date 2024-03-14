package klab.serialization;


import klab.serialization.BadAttributeValueException;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Deserialization input source This should ONLY include general methods for parsing. Do not include protocol-specific
 * methods; those should be in the protocol-specific classes.
 *
 * @version 1.0
 */


public class MessageInput {
    private static final byte DELIM = '\n'; //Delimiter to determine end of a stream
    private DataInputStream in; // Input Stream to read from

    /**
     * Constructs a new input source from an InputStream
     *
     * @param in byte input source
     * @throws NullPointerException if it is null
     * @throws IOException if there is an I/O problem
     */

    public MessageInput(InputStream in) throws NullPointerException, IOException {
        if (in == null) {
            throw new NullPointerException("InputStream is null");
        }
        this.in = new DataInputStream(new BufferedInputStream(in));
    }


    /**
     * read 4 bytes from inputStream
     *
     * @throws IOException if there is an I/O problem
     * @return byte array of 4 bytes
     */

    public byte[] read4bytes() throws IOException {
        byte b[] = new byte[4];

        for (int i = 0; i < 4; i++) {
            b[i] = (byte) in.read();
        }
        return b;
    }


    /**
     * read an unsigned int from inputStream
     *
     * @throws IOException if there is an I/O problem
     * @return long representing a unsigned int
     */


    public long readUnsignedInt() throws IOException {
        long l = 0;
        for (int i = 0; i < 4; i++) {
            l |= (in.read() & 0xFFL) << (24 - (i * 8));
        }
        return l;
    }


    /**
     * read a string from inputStream
     *
     * @throws IOException if there is an I/O problem
     * @return String read from buffer
     */


    public String readString() throws IOException {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        int nextByte;

        while ((nextByte = in.read()) != DELIM) {
            if (nextByte == -1) {
                throw new IOException("Premature end of stream");
            }
            buff.write(nextByte);
        }
        return buff.toString(StandardCharsets.US_ASCII);
    }


    /**
     * read a string of a given length from inputStream
     *
     * @param length length of string to read
     * @throws IOException if there is an I/O problem
     * @throws BadAttributeValueException if length is negative
     * @return String read from buffer
     */

    public String readLengthString(int length) throws IOException, BadAttributeValueException {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        int nextByte;

        for (int i = 0; i < length; i++) {
            nextByte = in.read();
            if (nextByte == -1) {
                throw new IOException("Premature end of stream");
            }
            buff.write(nextByte);
        }
        return buff.toString(StandardCharsets.US_ASCII.name());
    }


    /**
     * read a byte from inputStream
     *
     * @throws IOException if there is an I/O problem
     * @return byte read from buffer
     */

    public int read() throws IOException {
        try{
            return in.read();
        } catch (IOException e) {
            throw new IOException("Premature end of stream");
        }
    }

    /**
     * read a short from inputStream
     *
     * @throws IOException if there is an I/O problem
     * @return short read from buffer
     */

    public int readUnsignedShort() throws IOException {
        try{
            return in.readUnsignedShort();
        } catch (IOException e) {
            throw new IOException("Premature end of stream");
        }
    }

    /**
     * read a byte array of a given length from inputStream
     *
     * @param length length of byte array to read
     * @throws IOException if there is an I/O problem
     * @return byte array read from buffer
     */

    public byte[] readBytes(int length) throws IOException {
         try{
             return in.readNBytes(length);
         } catch (IOException e) {
             throw new IOException("Premature end of stream");
         }
    }


    /**
     * determines size of inputStream
     *
     * @throws IOException if there is an I/O problem
     * @return byte array read from buffer
     */


    public int size() throws IOException {
        return in.available();
    }

}
