package whitespell.logic;

/**
 * EndpointSpecification holds the interface (if any) for the current node, and also holds the variable name if it is a variable
 */
public class EndpointSpecification {
    public EndpointInterface apiInterface;
    public String varName;

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public EndpointSpecification(EndpointInterface apiInterface) {
        this.apiInterface = apiInterface;
    }
}
