package klab.serialization;


/**
 * Exception for signaling attribute validation problems
 * @version 1.0
 */
public class BadAttributeValueException extends Exception {

    /**
     * Serial version ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Name of attribute
     */
    public String attribute;

    /**
     * Construct exception
     * @param message error message
     * @param attribute name of attribute
     * @throws NullPointerException if message or attribute is null
     */
    public BadAttributeValueException(String message, String attribute) {
        super(message);
        if (message == null || attribute == null) {
            throw new NullPointerException("message or attribute is null");
        }
        this.attribute = attribute;
    }

    /**
     * Construct exception
     * @param message error message
     * @param attribute name of attribute
     * @param cause exception cause (may be null)
     * @throws NullPointerException if message or attribute is null
     */
    public BadAttributeValueException(String message, String attribute, Throwable cause) {
        super(message, cause);
        if (message == null || attribute == null) {
            throw new NullPointerException("message or attribute is null");
        }
        this.attribute = attribute;
    }

    /**
     * Get the name of the attribute
     * @return name of attribute
     */
    public String getAttribute() {
        return this.attribute;
    }

}
