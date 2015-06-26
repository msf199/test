package main.com.whitespell.peak.logic;

/**
 * EndpointSpecification holds the interface (if any) for the current node, and also holds the variable name if it is a variable
 */
public class EndpointSpecification {
    public EndpointHandler getEndpointInterface() {
        return endpointInterface;
    }

    private EndpointHandler endpointInterface;

    public String varName;

    public EndpointSpecification(EndpointHandler apiInterface) {
        this.endpointInterface = apiInterface;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }
}
