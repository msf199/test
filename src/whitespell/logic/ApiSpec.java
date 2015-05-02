package whitespell.logic;

/**
 * ApiSpec is a simple container for a REST interface and a set of names
 * for URL parameters that interface expects.
 */
public class ApiSpec {
    public ApiInterface apiInterface;
    public String[] argNames;

    public ApiSpec(ApiInterface apiInterface, String[] argNames) {
        this.apiInterface = apiInterface;
        this.argNames = argNames;
    }
}
