package metanode.serialization;

/**
 * Message Type
 *
 * @version 1.0
 */
public enum MessageType {
    /**
     * Request Nodes
     */
    RequestNodes(0),
    /**
     * Request metanodes
     */
    RequestMetaNodes(1),
    /**
     * Answer to requests
     */
    AnswerRequest(2),
    /**
     * Add nodes
     */
    NodeAdditions(3),
    /**
     * Add metanodes
     */
    MetaNodeAdditions(4),
    /**
     * Delete nodes
     */
    NodeDeletions(5),
    /**
     * Delete metanodes
     */
    MetaNodeDeletions(6);

    /**
     * Message code
     */

    private final int code;

    /**
     * Constructor for MessageType
     *
     * @param code message code
     */

    private MessageType(int code) {
        this.code = code;
    }

    /**
     * Get code for type
     *
     * @return type code
     */

    public int getCode() {
        return code;
    }

    /**
     * Get type for given code
     *
     * @param code code of type
     * @return type corresponding to code or null if bad code
     */

    public static MessageType getByCode(int code){
        code = code & 0x0F;
        return switch (code) {
            case 0 -> MessageType.RequestNodes;
            case 1 -> MessageType.RequestMetaNodes;
            case 2 -> MessageType.AnswerRequest;
            case 3 -> MessageType.NodeAdditions;
            case 4 -> MessageType.MetaNodeAdditions;
            case 5 -> MessageType.NodeDeletions;
            case 6 -> MessageType.MetaNodeDeletions;
            default -> null;
        };
    }

    /**
     * Get cmd for type
     *
     * @return type cmd
     */

    public String getCmd() {
        int temp = code;
        temp = temp & 0x0F;
        return switch (temp) {
            case 0 -> "RN";
            case 1 -> "RM";
            case 2 -> "AR";
            case 3 -> "NA";
            case 4 -> "MA";
            case 5 -> "ND";
            case 6 -> "MD";
            default -> null;
        };
    }

    /**
     * Get type for given cmd
     *
     * @param cmd cmd to find type of
     * @return type corresponding to cmd or null if bad cmd
     */


    public static MessageType getByCmd(String cmd) {
        if (cmd == null) {
            return null;
        }
        return switch (cmd) {
            case "AR" -> MessageType.AnswerRequest;
            case "MA" -> MessageType.MetaNodeAdditions;
            case "MD" -> MessageType.MetaNodeDeletions;
            case "NA" -> MessageType.NodeAdditions;
            case "ND" -> MessageType.NodeDeletions;
            case "RM" -> MessageType.RequestMetaNodes;
            case "RN" -> MessageType.RequestNodes;
            default -> null;
        };
    }

    /**
     * Get string representation of type
     *
     * @return string representation of type
     */

    public String toString() {
        return switch (this) {
            case RequestNodes -> "RequestNodes";
            case RequestMetaNodes -> "RequestMetaNodes";
            case AnswerRequest -> "AnswerRequest";
            case NodeAdditions -> "NodeAdditions";
            case MetaNodeAdditions -> "MetaNodeAdditions";
            case NodeDeletions -> "NodeDeletions";
            case MetaNodeDeletions -> "MetaNodeDeletions";
        };
    }
}
