package klab.serialization;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Serialzation output source This should ONLY include general methods for output. Do not include protocol-specific methods; those should be in the protocol-specific classes
 *
 * @version 1.0
 */
public class MessageOutput {
    private final DataOutputStream out; //Output stream to write to

    /**
     * Construct a new output source from an OutputStream
     *
     * @param out byte output sink
     * @throws NullPointerException if out is null
     */
    public MessageOutput(OutputStream out) throws NullPointerException {
        if (out == null) {
            throw new NullPointerException("OutputStream is null");
        }
        this.out = new DataOutputStream(out);
    }

    /**
     * writes 4 bytes to the MessageOutput's OutputStream
     *
     * @param fileID byte stream
     * @throws IOException if writing fails
     */

    public void write4Bytes(byte[] fileID) throws IOException {
        out.write(fileID, 0, 4);
    }


    /**
     * writes an unsigned int
     *
     * @param fileSize takes longs and writes unsigned ints to buffer
     * @throws IOException if writing fails
     */

    public void writeUnsignedInt(long fileSize) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt((int) fileSize);
        out.write(buffer.array());
        //out.write((int) fileSize);
    }

    /**
     * writes a string to the output stream
     *
     * @param fileName string to write to buffer
     * @throws IOException if writing fails
     */

    public void writeString(String fileName) throws IOException{
        for (int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            out.write(c);
        }
        out.write('\n');
    }

    /**
     * writes a string to the output stream
     *
     * @param fileName string to write to buffer
     * @param length length of string to write
     * @throws IOException if writing fails
     */

    public void writeLengthString(String fileName, int length) throws IOException {
        for (int i = 0; i < length; i++) {
            char c = fileName.charAt(i);
            out.write(c);
        }
    }

    /**
     * writes a byte to the output stream
     *
     * @param b byte to write to buffer
     * @throws IOException if writing fails
     */

    public void write(int b) throws IOException {
        out.write(b);
    }

    /**
     * writes a byte array to the output stream
     *
     * @param b byte array to write to buffer
     * @throws IOException if writing fails
     */


    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    /**
     * writes a short to the output stream
     *
     * @param s short to write to buffer
     * @throws IOException if writing fails
     */

    public void writeShort(int s) throws IOException {
        out.writeShort(s);
    }

}
