/**
 * This configuration contains the configurations that are known to the JVM and based on which the intelligence will calculate and cache your data.
 * The key always has to be present, and normally it should only be one object.
 * @type {{key: string[], variables: string[], imports: string[], cache: number}}
 */
var config = {
    keys: ["userid"],
    on: ["request", "intelligence"],
    bindTo: "/users/:userid/posts",
    euc: {
        allow: true,
        type: "array",
        eucLimit: 200
    }
};

/**
 * This function is executed from Java. In order to program like this, we need to start thinking differently about writing our code.
 * The action can be executed by an API endpoint, by our intelligence, or by an authorized server.
 * @param key is a JSON object with the unique key unlocker.
 * @param request is an object that contains GET parameters, payload, url, event and session and can be used for retrieving necessary details about the user
 */

function performAction(keys, request) {
    switch (origin) {
        case "intelligence" :
        {
            var posts = JSON.parse(MySQLConnections.get("appConnection").queue(1).executePS("NewsFeedQuery", keys[0], keys[0], config.eucLimit));

            EUC.sessionSpecificPath("/users/" + keys[0] + "/posts", posts, {
                index: post_id
            });

            /* for a news site this could be for example: */
            EUC.setGlobalPath("/newsarticles/", articles, {
                index: article_id
            });
            return;
        }
    /**
     * Whitespell can handle the request itself, or forward it to another server and adding the keys and any other variables to the payload.
     */
        case "request" :
        {
            var posts = JSON.parse(MySQLConnections.get("appConnection").queue(1).executePS("NewsFeedQuery", keys[0], keys[0], request.params.limit));
            setResponse(200, posts);
            return;
        }
    }
}