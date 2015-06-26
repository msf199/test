package main.com.whitespell.peak.logic;

import com.google.common.io.CharStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.logging.Logging;

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
     * Adds a handler for the EndpointDispatcher to dispatch.
     *
     * @param type         is the method, e.g. POST
     * @param apiInterface is the handler
     * @param pathSpec     is the path the handler is called on, e.g. /users/{user_id}/categories
     */

    public void addHandler(RequestType type, EndpointInterface apiInterface, String pathSpec, String... argNames) {
        EndpointSpecification spec = new EndpointSpecification(apiInterface);
        switch (type) {
            case GET:
                getStructure.addChildWithSubPath(pathSpec, spec, argNames);
                break;
            case POST:
                postStructure.addChildWithSubPath(pathSpec, spec, argNames);
                break;
            case PUT:
                putStructure.addChildWithSubPath(pathSpec, spec, argNames);
                break;
            case DELETE:
                delStructure.addChildWithSubPath(pathSpec, spec, argNames);
                break;
        }
    }

    ;

    /**
     * Execute the actual request and find the correct handler.
     *
     * @param request
     * @param response
     * @throws IOException
     */
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


            RequestObject context = new RequestObject(
                    request, response, urlVariables, request.getParameterMap(), null);

            JsonElement payload;
            try {
                payload = getPayload(request, response);
            } catch (Exception e) {
                Logging.log("High", e);
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NOT_VALID_JSON_PAYLOAD);
                return;
            }
            context.setPayload(payload);
            EndpointNode apiStructure = getApiStructure(method);

            if (request.getPathInfo() == null) {
                System.out.println("Received " + method + " request with empty path.");
                return;
            }


            EndpointNode.EndpointResult result = apiStructure.getBindingForSubPath(request.getPathInfo());
            if (result != null && result.getEndpointSpec() != null && result.getEndpointSpec().getEndpointInterface() != null) {
                urlVariables.putAll(result.getArgValues());

                // check the input and match it with the API interface keys before calling the handler.

                try {
                    // only check payload if a payload is expected
                    if(result.getEndpointSpec().getEndpointInterface().getPayloadInput().size() > 0) {
                        Safety.checkPayload(result.getEndpointSpec().getEndpointInterface().getPayloadInput(), context.getPayload());
                    }

                    Safety.checkParameterInput(result.getEndpointSpec().getEndpointInterface().getParameterInput(), context.getParameterMap());

                    Safety.checkUrlInput(result.getEndpointSpec().getEndpointInterface().getUrlInput(), context.getUrlVariables());
                } catch(InputNotValidException p) {
                    context.throwHttpError(result.getEndpointSpec().getEndpointInterface().getClass().getSimpleName(), StaticRules.ErrorCodes.NULL_VALUE_FOUND,
                            p.getDetailMessage());
                    return;
                }
                result.getEndpointSpec().getEndpointInterface().call(context);
            } else {
                context.throwHttpError(this.getClass().getSimpleName(), StaticRules.ErrorCodes.NO_ENDPOINT_FOUND);
                return;
            }
        } catch (Exception e) {

            Logging.log("Low", e);
        }
    }

    private EndpointNode getApiStructure(String method) {
        if (method.equalsIgnoreCase("get")) {
            return getStructure;
        } else if (method.equalsIgnoreCase("post")) {
            return postStructure;
        } else if (method.equalsIgnoreCase("put")) {
            return putStructure;
        } else if (method.equalsIgnoreCase("delete")) {
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

    /**
     * Supported request methods.
     */
    public enum RequestType {
        GET,
        POST,
        PUT,
        DELETE
    }
}
