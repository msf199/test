package whitespell.logic;

/**
 * ApiSpec is a simple container for a REST interface and a set of names
 * for URL parameters that interface expects.
 */
public class EndpointSpecification {
    public EndpointInterface apiInterface;
    public String[] argNames;

    public EndpointSpecification(EndpointInterface apiInterface, String[] argNames) {
        this.apiInterface = apiInterface;
        this.argNames = argNames;
    }
}
