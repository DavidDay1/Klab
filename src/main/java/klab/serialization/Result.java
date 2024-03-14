package klab.serialization;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

//import org.apache.commons.lang3.StringUtils;

/**
 * Represents a single search result
 *
 * @version 1.0
 */
public class Result {
    private byte[] fileId; //id of the file
    private long fileSize; //size of the file
    private String filename; //name of the file

    /**
     * Constructs a Result from given input source
     *
     * @param in input source to parse
     * @throws IOException if in is null or I/O problem occurs
     * @throws BadAttributeValueException if any parsed value fails validation
     */
    public Result(MessageInput in) throws IOException, BadAttributeValueException {
        if (in == null) {
            throw new IOException("MessageInput is invalid");
        }

        try{
        setFileID(in.read4bytes());
        setFileSize(in.readUnsignedInt());
        setFileName(in.readString());
        } catch (IOException e) {
            throw new IOException("Error reading from input source", e);
        }


    }

    /**
     * Constructs a Result from given attributes
     *
     * @param fileID file ID
     * @param fileSize file size
     * @param filename file name
     * @throws BadAttributeValueException if any parameter fails validation
     */
    public Result(byte[] fileID, long fileSize, String filename) throws BadAttributeValueException {
        setFileID(fileID);
        setFileSize(fileSize);
        setFileName(filename);
    }

    /**
     * Serialize to given output sink
     *
     * @param out output sink to serialize to
     * @throws IOException if out is null or I/O problem
     */
    public void encode(MessageOutput out) throws IOException {
        if (out == null) {
            throw new IOException("MessageOutput is invalid");
        }
        out.write4Bytes(fileId);
        out.writeUnsignedInt(fileSize);
        out.writeString(filename);
    }

    /**
     * Returns a String representation
     *
     * @return String representation
     */
    public String toString() {
        String fileIdString = "";
        for (int i = 0; i < this.fileId.length; i++) {
            fileIdString += String.format("%02X", this.fileId[i]);
        }
        return "Result: FileID=" + fileIdString + " FileSize=" + this.fileSize + " bytes " + "FileName" +
                "=" + this.filename;
    }
    

    /**
     * Get file ID
     *
     * @return file ID
     */
    public byte[] getFileID() {
        return this.fileId;
    }

    /**
     * Set file ID
     *
     * @param fileID new file ID
     * @return this Result with new file ID
     * @throws BadAttributeValueException if fileID fails validation
     */
    public final Result setFileID(byte[] fileID) throws BadAttributeValueException {
        if (fileID == null) {
            throw new BadAttributeValueException("fileID is null", "fileID");
        } else if (fileID.length != 4) {
            throw new BadAttributeValueException("fileID is not 4 bytes", "fileID");
        } else {
            for (int i = 0; i < fileID.length; i++) {
                if (fileID[i] < 0) {
                    fileID[i] = (byte) (fileID[i] & 0xff);
                }
            }
            this.fileId = fileID;
            return this;
        }
    }

    /**
     * Get file size
     *
     * @return file size
     */
    public long getFileSize() {
        return this.fileSize;
    }

    /**
     * Set file size
     *
     * @param fileSize new file size
     * @return this Result with new file size
     * @throws BadAttributeValueException if fileSize fails validation
     */
    public final Result setFileSize(long fileSize) throws BadAttributeValueException {
        if (fileSize < 0) {
            throw new BadAttributeValueException("fileSize is negative", "fileSize");
        } else if (fileSize > 4294967295L) {
            throw new BadAttributeValueException("fileSize is too large", "fileSize");
        } else {
            this.fileSize = fileSize;
            return this;
        }
    }

    /**
     * Get file name
     *
     * @return file name
     */
    public String getFileName() {
        return this.filename;
    }

    /**
     * Set file name
     *
     * @param filename file name
     * @return this Result with new file name
     * @throws BadAttributeValueException if fileName is null or fails validation
     */
    public final Result setFileName(String filename) throws BadAttributeValueException {
        if (filename == null || filename.length() == 0) {
            throw new BadAttributeValueException("filename is null or empty", "filename");
        }
        byte[] bytes = filename.getBytes(StandardCharsets.US_ASCII);
        String asciiString = new String(bytes, StandardCharsets.US_ASCII);

        for (int i = 0; i < asciiString.length(); i++) {
            char c = asciiString.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                if (c != '.' && c != '-' && c != '_') {
                    throw new BadAttributeValueException("filename contain non-alphanumeric characters", "filename");
                }
            }
        }
        this.filename = asciiString;
        return this;
    }

    /**
     * Get size of Result
     *
     * @return size of Result
     */

    public int getSize(){
        return 9 + filename.length();
    }

    /**
     * Compares Result objects
     * @param o object to compare
     * @return true if objects are equal, false otherwise
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return fileSize == result.fileSize && Arrays.equals(fileId, result.fileId) && filename.equals(result.filename);
    }

    /**
     * Returns a hash code value for the object
     *
     * @return hash code value
     */

    @Override
    public int hashCode() {
        int result = Objects.hash(fileSize, filename);
        result = 31 * result + Arrays.hashCode(fileId);
        return result;
    }
}
