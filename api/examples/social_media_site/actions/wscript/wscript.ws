import whitespell;

sessionscript GetNewsfeed() {

set GetNewsFeed() {
    requireDatabases("appConnection");
    executeOn("request","keys");
    bindToPath("{me}/users/:userid");
    bindToKeys("$userid");
}

void onRequest(key, request) {
// regular way of handling the session

}

void onKeys(keys, values) {

}

}