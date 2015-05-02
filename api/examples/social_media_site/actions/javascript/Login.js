/**
 * This configuration contains the configurations that are known to the JVM and based on which the intelligence will calculate and cache your data.
 * @type {{key: string[], variables: string[], imports: string[], cache: number}}
 */
var config = {
    key: ["$session->id"],
    on: ["request"],
    euc: {
        allow: false
    }
}

/**
 * This function is executed from Java.
 * @param keys is a JSON object with the keys and their associated values
 * @param variables is a JSON object with all extra parsed parameters.
 */



var action = function(keys, user, origin) {
    var loginObject = JSON.parse(MySQLConnections.get("appConnection").queue(1).executePS("NewsFeedQuery", key[0], key[0], 200));
    var userId = loginObject[0]["userid"];
    EUC.setSessionKey("userid", userid);
}