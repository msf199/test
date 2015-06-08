package whitespell.logic;

import whitespell.logic.ApiSpec;

import java.util.HashMap;
import java.util.Map;

/**
 * PathNode is a tree representing a path hierarchy allowing for '?' wildcard substitution
 * for each path component. Call getBindingForSubPath(subPath) to traverse the tree and
 * bind to an API handler with populated argument values or addChildWithSubPath(subPath)
 * to add a binding to the tree.
 *
 * Note that the tree traversal does NOT backtrack, so given the following set of bindings:
 * HandlerA: /foo/bar/?/bat
 * HandlerB: /foo/bar/baz/bas
 *
 * .../foo/bar/baz/bat will NOT match the top entry, as /foo/bar/? and /foo/bar/baz form
 * separate branches in the tree.
 */
public class PathNode {
    private Map<String, PathNode> children;
    private ApiSpec apiSpec = null;

    public PathNode() {
        children = new HashMap<String, PathNode>();
    }

    public Map<String, PathNode> getChildren() {
        return this.children;
    }

    public void putChild(String name, PathNode child) {
        children.put(name, child);
    }

    /**
     * Traverses the tree and returns the ApiSpec matching
     * the given sub-path as well as the values of any matched
     * arguments, or null if no ApiSpec was found.
     * @param subPath the sub-path being called, relative to this node
     * @return a PathNodeResult containing the bound ApiSpec and all
     * matched arguments, or null if no ApiSpec was found.
     */
    public PathNodeResult getBindingForSubPath(String subPath) {

        System.out.println("SubPath: " + subPath);

        for(String s : children.keySet()) {
            System.out.println("Child: " + s);
        }

        String[] pathComponents = subPath.split("/");

        PathNode current = this;
        HashMap<String, String> argValues = new HashMap<String, String>();
        for (String pathComponent : pathComponents) {

            if(pathComponent == null || pathComponent.length() < 1) {
                continue;
            }

            if (current.getChildren().containsKey(pathComponent)) {
                System.out.println("Found path component for " + pathComponent);
                current = current.getChildren().get(pathComponent);
            } else if (current.getChildren().containsKey("?")) {
                current = current.getChildren().get("?");
                argValues.put(current.getApiSpec().argNames[argValues.size()], pathComponent);
            } else {
                return null;
            }
        }
        return new PathNodeResult(current.getApiSpec(), argValues);
    }

    /**
     * Traverses the trie and adds the given apiSpec as a leaf node
     * with a sub-path relative to this PathNode, or replaces an existing
     * leaf node with the same sub-path.
     * @param subPath the sub-path to bind to, relative to this node
     * @param apiSpec the ApiSpec to bind
     */
    public void addChildWithSubPath(String subPath, ApiSpec apiSpec) {
        String[] pathComponents = subPath.split("/");

        PathNode current = this;

        // iterate over all the components in the path

        for(int i = 0; i < pathComponents.length; i++) {

            if (pathComponents[i] == null || pathComponents[i].length() < 1) {
                continue;
            }

            System.out.println("Iterating over path component: " + pathComponents[i]);

            //iterate over children, create child if neceesary, and enter into the child
            if (!current.getChildren().containsKey(pathComponents[i])) {
                current.putChild(pathComponents[i], new PathNode());
            }

            current = current.getChildren().get(pathComponents[i]);

            if (i == (pathComponents.length - 1)) {
                System.out.println("Set API spec in " + pathComponents[i]);
                // when we are at the last of the loop, insert the current pathnode as the api spec
                current.setApiSpec(apiSpec);
            }

        }

    }

    /**
     * Container for PathNode traversals that includes both the API specification
     * and the values for all matched arguments.
     */
    public class PathNodeResult {
        private ApiSpec apiSpec;
        private Map<String, String> argValues;

        public PathNodeResult(ApiSpec apiSpec, Map<String, String> argValues) {
            this.apiSpec = apiSpec;
            this.argValues = argValues;
        }

        public ApiSpec getApiSpec() {
            return apiSpec;
        }

        public Map<String, String> getArgValues() {
            return argValues;
        }
    }

    protected ApiSpec getApiSpec() {
        return apiSpec;
    }

    protected void setApiSpec(ApiSpec apiSpec) {
        this.apiSpec = apiSpec;
    }
}
