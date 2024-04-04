package metanode.serialization;


/**
 * Error Type
 *
 * @version 1.0
 */
public enum ErrorType {
    /**
     * No error
     */
    None(0),

    /**
     * System error
     */
    System(10),

    /**
     * Unexpected packet type
     */
    IncorrectPacket(20);


    /**
     * Error code
     */

    private final int code;


    /**
     * Constructor for ErrorType
     *
     * @param code error code
     */

    private ErrorType(int code) {
        this.code = code;
    }


    /**
     * Get error for given code
     *
     * @param code code of error
     * @return error corresponding to code or null if bad code
     */
    public static ErrorType getByCode(int code) {
        return switch (code) {
            case 0 -> ErrorType.None;
            case 10 -> ErrorType.IncorrectPacket;
            case 20 -> ErrorType.System;
            default -> null;
        };

    }

    /**
     * Get code of error
     *
     * @return error code
     */

    public int getCode() {
        return code;
    }


    /**
     * Get string representation of error
     *
     * @return string representation of error
     */
    public String toString() {
        return switch (code) {
            case 0 -> "None";
            case 10 -> "System";
            case 20 -> "IncorrectPacket";
            default -> null;
        };
    }

}