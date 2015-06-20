package whitespell.logic;

import java.util.HashMap;
import java.util.Map;

/**
 * The EndpointNode class is part of a tree of nodes and children.
 * Each node can contain an EndpointSpecification (which will be called if the endpoint is called) and can also have children.
 * Each child can be a static word such as 'statistics' or a variable (defined as '?')
 *
 * ===== children:
 * GET has a root EndpointNode, for which will for example have children:
 * 1. users, 2. statistics
 * users will then have the children /users/? for a specific user, or /users can also be an endpoint.
 */
public class EndpointNode {

    // the children of this node, e.g. /users, or /users/?
    private Map<String, EndpointNode> children;

    // the endpoint specification (the handler plus the url variables)
    private EndpointSpecification endpointSpecification = null;

    public EndpointNode() {
        children = new HashMap<>();
    }

    // get all the children for this node
    public Map<String, EndpointNode> getChildren() {
        return this.children;
    }

    // add a node to the tree
    public void putChild(String name, EndpointNode child) {
        children.put(name, child);
    }

    /**
     * This loops through the current path and finds the right endpoint node for the path.
     */
    public EndpointResult getBindingForSubPath(String subPath) {

        String[] pathComponents = subPath.split("/");

        EndpointNode current = this;
        HashMap<String, String> argValues = new HashMap<>();
        for (String pathComponent : pathComponents) {

            if(pathComponent == null || pathComponent.length() < 1) {
                continue;
            }

            // first we check if the input matches an exact new child

            if (current.getChildren().containsKey(pathComponent)) {
                current = current.getChildren().get(pathComponent);

            // otherwise it must be a variable if a variable exists.
            } else if (current.getChildren().containsKey("?")) {
                current = current.getChildren().get("?");
                argValues.put(current.getEndpointSpecification().varName, pathComponent);
            } else {
                return null;
            }
        }
        return new EndpointResult(current.getEndpointSpecification(), argValues);
    }

    /**
     * This loops through the list of path components (e.g. /statistics/users) and adds it to the correct endpoint node.
     */
    public void addChildWithSubPath(String subPath, EndpointSpecification endpointSpecification, String... argNames) {
        String[] pathComponents = subPath.split("/");

        EndpointNode current = this;

        // iterate over all the components in the path

        int lastVar = -1;

        for(int i = 0; i < pathComponents.length; i++) {

            if (pathComponents[i] == null || pathComponents[i].length() < 1) {
                continue;
            }

            //iterate over children, create child if neceesary, and enter into the child
            if (!current.getChildren().containsKey(pathComponents[i])) {
                current.putChild(pathComponents[i], new EndpointNode());
            }

            current = current.getChildren().get(pathComponents[i]);

            if (i == (pathComponents.length - 1)) {
                // when we are at the last of the loop, insert the current node
                current.setEndpointSpecification(endpointSpecification);
            }

            if(pathComponents[i].equals("?")) {
                // set the name of the variable in the current endpoint specification, even if no interface exists, always incremental

                if(current.getEndpointSpecification() == null) {
                    current.setEndpointSpecification(new EndpointSpecification(null)); // specification does not have to contain an interface, can also be just variable
                }
                current.getEndpointSpecification().setVarName(argNames[lastVar+1]);
            }

        }

    }

    /**
     * Container for PathNode traversals that includes both the API specification
     * and the values for all matched arguments.
     */

    public class EndpointResult {
        private EndpointSpecification endpointSpec;
        private Map<String, String> argValues;

        public EndpointResult(EndpointSpecification apiSpec, Map<String, String> argValues) {
            this.endpointSpec = apiSpec;
            this.argValues = argValues;
        }

        public EndpointSpecification getEndpointSpec() {
            return endpointSpec;
        }

        public Map<String, String> getArgValues() {
            return argValues;
        }
    }

    protected EndpointSpecification getEndpointSpecification() {
        return endpointSpecification;
    }

    protected void setEndpointSpecification(EndpointSpecification endpointSpecification) {
        this.endpointSpecification = endpointSpecification;
    }
}
