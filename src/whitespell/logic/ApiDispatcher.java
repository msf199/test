package whitespell.logic;

import com.google.common.io.CharStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

public class ApiDispatcher extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(ApiDispatcher.class);

    private static PathNode putStructure = new PathNode();
    private static PathNode delStructure = new PathNode();
    private static PathNode postStructure = new PathNode();
    private static PathNode getStructure = new PathNode();

    private HashMap<String, String> urlVariables = new HashMap<String, String>();

    /**
     * Supported request methods.
     */
    public enum RequestType {
        GET,
        POST,
        PUT,
        DELETE
    };

    /**
     * Adds a handler for dispatch.
     * @param type a supported request type (e.g. PUT)
     * @param apiInterface the BaseHandler class that will handle the request
     * @param pathSpec a String specifying a supported URL and parameter names, with the format
     *                 /requiredPath/?/?.
     * @param argNames one or more argument names that map to pathSpec wildcards
     */
    public void addHandler(RequestType type, ApiInterface apiInterface, String pathSpec, String... argNames) {
        ApiSpec spec = new ApiSpec(apiInterface, argNames);
        switch (type) {
            case GET:
                getStructure.addChildWithSubPath(pathSpec, spec);
                break;
            case POST:
                postStructure.addChildWithSubPath(pathSpec, spec);
                break;
            case PUT:
                putStructure.addChildWithSubPath(pathSpec, spec);
                break;
            case DELETE:
                delStructure.addChildWithSubPath(pathSpec, spec);
                break;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        callHandler("get", request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        callHandler("post", request, response);
    }
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        callHandler("put", request, response);
    }
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        callHandler("delete", request, response);
    }

    /**
     * Dispatches a request to the handler that best matches the request.
     *
     * In the case of conflicts, this function prefers the longest matching path.
     *
     * Note that, in the case that a path element could be either a wildcard or
     * an exact match, dispatch will branch on the exact match and not backtrack.
     * In other words, given the following two handlers:
     * HandlerA: /foo/bar/?
     * HandlerB: /foo/bar/baz
     * ...HandlerB will accept requests to /foo/bar/baz properly, though it is
     * strongly encouraged not to support this case.
     *
     * However, if each of these are given leaf nodes:
     * HandlerA: /foo/bar/?/baz
     * HandlerB: /foo/bar/bat/bar
     * ..then /foo/bar/bat/baz will NOT be routed.
     *
     * @param method the request method
     * @param request
     * @param response
     * @throws java.io.IOException
     */

    private void callHandler(String method, HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonElement payload = getPayload(request, response);
        RequestContext context = new RequestContext(
                request, response, urlVariables, request.getParameterMap(), payload);
        PathNode apiStructure = getApiStructure(method);

        if (request.getPathInfo() == null) {
            logger.warn("Received " + method + " request with empty path.");
            return;
        }

        PathNode.PathNodeResult result = apiStructure.getBindingForSubPath(request.getPathInfo());
        if (result != null) {
            urlVariables.putAll(result.getArgValues());
            result.getApiSpec().apiInterface.call(context);
        }
    }

    private PathNode getApiStructure(String method) {
        if(method.equalsIgnoreCase("get")) {
            return getStructure;
        } else if(method.equalsIgnoreCase("post")) {
            return postStructure;
        } else if(method.equalsIgnoreCase("put")) {
            return putStructure;
        } else if(method.equalsIgnoreCase("delete")) {
            return delStructure;
        }

        return null;
    }

    /**
     * Utility function that returns a JsonElement embedded in the payload of the request,
     * or null if the body is missing or improper JSON.
     * @param request
     * @param response
     * @return
     * @throws com.google.gson.JsonParseException if there is an error parsing JSON from the payload
     * @throws IllegalStateException if the body is not a JSON object
     * @throws java.io.IOException if the payload could not be read
     */
    private JsonElement getPayload(HttpServletRequest request, HttpServletResponse response)
            throws JsonParseException, IllegalStateException, IOException {
        String body = getBody(request);
        System.out.println(body);
        return new JsonParser().parse(body);
    }

    private String getBody(HttpServletRequest request) throws IOException {
        return CharStreams.toString(request.getReader());
    }
}
