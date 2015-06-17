package whitespell.logic;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import whitespell.StaticRules;
import whitespell.logic.logging.Logging;
import whitespell.model.ErrorObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Preprocessed object containing all request data, which is parsed as a parameter to the endpoint handler.
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

    public void throwHttpError(StaticRules.ErrorCodes error) {
            // set the HTTP status code to the correct status code
            this.response.setStatus(error.getHttpStatusCode());

            //construct the JSON object to return
            Gson g = new Gson();
            ErrorObject eo = new ErrorObject();
            eo.setHttpStatusCode(error.getHttpStatusCode());
            eo.setErrorId(error.getErrorId());
            eo.setErrorMessage(error.getErrorMessage());
            String errorObject = g.toJson(eo);

            // write the response to the writer
        try {
            this.response.getWriter().write(errorObject);
            this.response.getWriter().close(); // ensure no other objects can be written to a closed header because the error was thrown
        } catch (IOException e) {
            Logging.log("Low", e);
        }
    }
}
