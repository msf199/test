package whitespell.peakapi;

import whitespell.peakapi.endpoints.statistics.GetUserSignups;
import whitespell.peakapi.endpoints.users.*;
import whitespell.model.baseapi.WhitespellWebServer;
import whitespell.logic.ApiDispatcher;
import whitespell.peakapi.endpoints.users.UserFollowAction;
import whitespell.peakapi.endpoints.users.content.AddNewContent;
import whitespell.peakapi.endpoints.users.content.RequestContent;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/4/15
 *         whitespell.peakapi
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
        dispatcher.addHandler(ApiDispatcher.RequestType.POST, new CreateUser(), "/users");

        //test
        dispatcher.addHandler(ApiDispatcher.RequestType.GET, new GetUserSignups(), "/statistics/signups/");

        //authentication API
        dispatcher.addHandler(ApiDispatcher.RequestType.POST, new AuthenticationRequest(), "/authentication");

        // profile API /users/{userid}/profile

        // Interests API /users/{userid}/profile/interests/

        // search API {/search?q=xyz}

        // newsfeed {/users/{userid}/newsfeed}

        // trending {/users/{userid}/trending}

        // content API /content/
        dispatcher.addHandler(ApiDispatcher.RequestType.POST, new AddNewContent(), "/content/?", "user_id");
        dispatcher.addHandler(ApiDispatcher.RequestType.GET, new RequestContent(), "/content/");

        // following API /users/{userid}/following
        dispatcher.addHandler(ApiDispatcher.RequestType.POST, new UserFollowAction(), "/following/?", "user_id");

        // saved content API {/users/{userid}/saved_content}

        // notifications API {/users/{userid}/notifications}

        // search API {/search?q=xyz}
    }


}
