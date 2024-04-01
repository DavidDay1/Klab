package metanode.serialization;


public enum ErrorType {
    None(0),
    System(1),
    IncorrectPacket(2);


    private final int code;

    private ErrorType(int code) {
        this.code = code;
    }


    public static ErrorType getByCode(int code) {
        return switch (code) {
            case 0 -> ErrorType.IncorrectPacket;
            case 1 -> ErrorType.None;
            case 2 -> ErrorType.System;
            default -> null;
        };

    }

    public int getCode() {
        return code;
    }

}