1. User logs in by requesting /authenticate
2. /authenticate returns the user id to use for API calls
3. request to /users/{userid} returns your user object if userid matches $session->userid
4. request to /users/{userid}/newsfeed returns your personal newsfeed