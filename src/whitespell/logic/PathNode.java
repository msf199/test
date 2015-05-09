package whitespell.logic;

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
        children = new HashMap<>();
    }

    /**
     * Traverses the tree and returns the ApiSpec matching
     * the given sub-path as well as the values of any matched
     * arguments, or null if no ApiSpec was found.
     * @param subPath the sub-path being called, relative to this node
     * @return a PathNodeResult containing the bound ApiSpec and all
     * matched arguments, or null if no ApiSpec was found.
     */
    @Deprecated
    public PathNodeResult getBindingForSubPath(String subPath) {
        String[] pathComponents = subPath.split("/");

        PathNode current = this;
        HashMap<String, String> argValues = new HashMap<>();
        for (String pathComponent : pathComponents) {
            if (children.containsKey(pathComponent)) {
                current = children.get(pathComponent);
            } else if (children.containsKey("?")) {
                current = children.get("?");
                argValues.put(current.getApiSpec().argNames[argValues.size()], pathComponent);
            } else {
                return null;
            }
        }
        return new PathNodeResult(current.getApiSpec(), argValues);
    }

    /**
     * Gets the {@link whitespell.logic.PathNode.PathNodeResult} for the given path.
     * @param path  the path to find results for
     * @return a {@link whitespell.logic.PathNode.PathNodeResult} containing
     * the bound ApiSpec and all matched arguments, or null if no Apispec was found.
     */
    public PathNodeResult getPathNodeResult(String path) {
        String[] args = path.split("/");


        StringBuilder pathNodeKey = new StringBuilder();

        pathNodeKey.append(path);


        PathNode current = children.get(pathNodeKey.toString());

        if (current == null) {
            throw new RuntimeException("current pathnode is null!");
        }

        HashMap<String, String> argValues = new HashMap<>();
        if(current.getApiSpec().argNames.length > 0) {
            for (int index = 2; index < args.length; index++) {
                argValues.put(current.getApiSpec().argNames[index - 2], args[index]);
            }
        }

        return new PathNodeResult(current.getApiSpec(), argValues);
    }

    /**
     * Deprecated due to bug with path routing. See 'addPathNode'.
     *
     * Traverses the trie and adds the given apiSpec as a leaf node
     * with a sub-path relative to this PathNode, or replaces an existing
     * leaf node with the same sub-path.
     * @param subPath the sub-path to bind to, relative to this node
     * @param apiSpec the ApiSpec to bind
     */
    @Deprecated
    public void addChildWithSubPath(String subPath, ApiSpec apiSpec) {
        String[] pathComponents = subPath.split("/");

        PathNode current = this;
        for (String pathComponent : pathComponents) {
            if (pathComponent.isEmpty()) {
                continue;
            }
            if (!children.containsKey(pathComponent)) {
                children.put(pathComponent, new PathNode());
            }
            current = children.get(pathComponent);
        }

        if (current == null) {
            throw new RuntimeException("current pathnode is null!");
        }

        current.setApiSpec(apiSpec);
    }

    /**
     * Adds the path node and {@link ApiSpec} for the given path identifier.
     * @param identifier the path identifier to bind to
     * @param spec  the ApiSpec to bind
     */
    public void addPathNode(String identifier, ApiSpec spec) {
        int argCount = spec.argNames != null && spec.argNames.length > 0 ? spec.argNames.length : 0;

        StringBuilder pathNodeKey = new StringBuilder();
        if (argCount >= 1) {
            for (int index = 1; index < argCount; index++) {
                pathNodeKey.append("/?");
            }
        }
        String key = identifier + pathNodeKey.toString();
        if (!children.containsKey(key)) {
            children.put(key, new PathNode());
        }

        PathNode current = children.get(key);
        current.setApiSpec(spec);
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
