package metanode.serialization;

public enum MessageType {
    AnswerRequest(0),
    MetaNodeAdditions(1),
    MetaNodeDeletions(2),
    NodeAdditions(3),
    NodeDeletions(4),
    RequestMetaNodes(5),
    RequestNodes(6);

    private final int code;

    private MessageType(int code) {
        this.code = code;
    }


    public int getCode() {
        return code;
    }

    public String getCmd() {
        return switch (code) {
            case 0 -> "AnswerRequest";
            case 1 -> "MetaNodeAdditions";
            case 2 -> "MetaNodeDeletions";
            case 3 -> "NodeAdditions";
            case 4 -> "NodeDeletions";
            case 5 -> "RequestMetaNodes";
            case 6 -> "RequestNodes";
            default -> null;
        };
    }


    public static MessageType getByCmd(String cmd) {
        return switch (cmd) {
            case "AnswerRequest" -> MessageType.AnswerRequest;
            case "MetaNodeAdditions" -> MessageType.MetaNodeAdditions;
            case "MetaNodeDeletions" -> MessageType.MetaNodeDeletions;
            case "NodeAdditions" -> MessageType.NodeAdditions;
            case "NodeDeletions" -> MessageType.NodeDeletions;
            case "RequestMetaNodes" -> MessageType.RequestMetaNodes;
            case "RequestNodes" -> MessageType.RequestNodes;
            default -> null;
        };
    }
}
