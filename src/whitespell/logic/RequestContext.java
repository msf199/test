package whitespell.logic;

import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Container for the preprocessed context of an API request.
 */
public class RequestContext {
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final Map<String, String> urlVariables;
    private final Map<String, String[]> parameterMap;
    private final JsonElement payload;

    public RequestContext(HttpServletRequest request,
                          HttpServletResponse response,
                          Map<String, String> urlVariables,
                          Map<String, String[]> parameterMap,
                          JsonElement payload) {
        this.request = request;
        this.response = response;
        this.urlVariables = urlVariables;
        this.parameterMap = parameterMap;
        this.payload = payload;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public Map<String, String> getUrlVariables() {
        return urlVariables;
    }

    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    public JsonElement getPayload() {
        return payload;
    }
}
