package whitespell.logic;

import com.google.common.io.CharStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import whitespell.logic.logging.Logging;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

public class EndpointDispatcher extends HttpServlet {

    private static EndpointNode putStructure = new EndpointNode();
    private static EndpointNode delStructure = new EndpointNode();
    private static EndpointNode postStructure = new EndpointNode();
    private static EndpointNode getStructure = new EndpointNode();

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

    public void addHandler(RequestType type, EndpointInterface apiInterface, String pathSpec, String... argNames) {
        EndpointSpecification spec = new EndpointSpecification(apiInterface, argNames);
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


    private void callHandler(String method, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            JsonElement payload = getPayload(request, response);
            RequestObject context = new RequestObject(
                    request, response, urlVariables, request.getParameterMap(), payload);
            EndpointNode apiStructure = getApiStructure(method);

            if (request.getPathInfo() == null) {
                System.out.println("Received " + method + " request with empty path.");
                return;
            }


            EndpointNode.EndpointResult result = apiStructure.getBindingForSubPath(request.getPathInfo());
            if (result != null) {
                urlVariables.putAll(result.getArgValues());
                result.getApiSpec().apiInterface.call(context);
            } else {
                response.getWriter().write("Error: Handler for path not found");
                response.setStatus(404);
                response.getWriter().close();
                return;
            }
        }catch(Exception e) {
            Logging.log("Low", e);
        }
    }

    private EndpointNode getApiStructure(String method) {
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
     * Converts the body to a JsonElement
     */
    private JsonElement getPayload(HttpServletRequest request, HttpServletResponse response)
            throws JsonParseException, IllegalStateException, IOException {
        String body = getBody(request);
        return new JsonParser().parse(body);
    }

    private String getBody(HttpServletRequest request) throws IOException {
        return CharStreams.toString(request.getReader());
    }
}
