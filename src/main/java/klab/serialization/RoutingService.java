package klab.serialization;

/**
 * Routing Service
 * @version 1.0
 */

public enum RoutingService {
    /**
     * Routing service breadth first method
     */
    BREADTHFIRST (0),
    /**
     * Routing service depth first method
     */
    DEPTHFIRST (1);

    private int methodCode; //Routing service method

    /**
     * Constructs a RoutingService from given method code
     *
     * @param methodCode value to represent the routing service used
     */

    private RoutingService(int methodCode) {
        this.methodCode = methodCode;
    }

    /**
     * Get code for routing service
     *
     * @return routing service code
     */

    public int getCode() {
        return methodCode;
    }

    /**
     * Get routing service for given code
     *
     * @param code code of routing service
     * @return routing service corresponding to code
     * @throws BadAttributeValueException if bad code value
     */

    public static RoutingService getRoutingService(int code) throws BadAttributeValueException {
        if (code == 0) {
            return RoutingService.BREADTHFIRST;
        } else if (code == 1) {
            return RoutingService.DEPTHFIRST;
        } else {
            throw new BadAttributeValueException("Invalid Routing Service Code", "Routing Service Code");
        }
    }



}
