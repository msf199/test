package whitespell.sample.MyApplication;

import whitespell.sample.MyApplication.endpoints.users.*;
import whitespell.model.baseapi.WhitespellWebServer;
import whitespell.logic.ApiDispatcher;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/4/15
 *         whitespell.sample.MyApplication
 */
public class MyEndpoints extends WhitespellWebServer {

    public MyEndpoints(String apiKey) {
        super(apiKey);
    }

    /**
     * The endpoints that you schedule here are handled as follows: When the user first does a REST call to the API a session ID is returned as a header.
     * The user is already connected using socket.io, and will send a "identificationEvent" to identify the connection with the session.
     *
     * @param dispatcher
     */
    @Override
    protected void scheduleEndpoints(ApiDispatcher dispatcher) {

        //user creation
        dispatcher.addHandler(ApiDispatcher.RequestType.POST, new CreateUser(), "/users/");

        //authentication API
        dispatcher.addHandler(ApiDispatcher.RequestType.POST, new AuthenticationRequest(), "/authentication/");

        // profile API /users/{userid}/profile

        // search API {/search?q=xyz}

        // newsfeed {/users/{userid}/newsfeed}

        // trending {/users/{userid}/trending}

        // following API /users/{userid}/following

        dispatcher.addHandler(ApiDispatcher.RequestType.POST, new UserFollowAction(), "/users/?/following/", "user_id");

        // saved content API {/users/{userid}/saved_content}

        // notifications API {/users/{userid}/notifications}
    }


}
