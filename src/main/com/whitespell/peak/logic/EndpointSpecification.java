package main.com.whitespell.peak.logic;

/**
 * EndpointSpecification holds the interface (if any) for the current node, and also holds the variable name if it is a variable
 */
public class EndpointSpecification {
    public EndpointInterface getEndpointInterface() {
        return endpointInterface;
    }

    private EndpointInterface endpointInterface;

    public String varName;

    public EndpointSpecification(EndpointInterface apiInterface) {
        this.endpointInterface = apiInterface;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }
}
