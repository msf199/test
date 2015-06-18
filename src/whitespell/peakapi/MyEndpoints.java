package whitespell.peakapi;

import whitespell.logic.EndpointDispatcher;
import whitespell.model.baseapi.WhitespellWebServer;
import whitespell.peakapi.endpoints.content.AddNewContent;
import whitespell.peakapi.endpoints.content.RequestContent;
import whitespell.peakapi.endpoints.content.categories.AddCategory;
import whitespell.peakapi.endpoints.content.categories.RequestCategories;
import whitespell.peakapi.endpoints.content.types.AddContentType;
import whitespell.peakapi.endpoints.content.types.RequestContentTypes;
import whitespell.peakapi.endpoints.statistics.GetUserSignups;
import whitespell.peakapi.endpoints.users.*;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/4/15
 *         whitespell.peakapi
 */
public class MyEndpoints extends WhitespellWebServer {


    /**
     * The endpoints that you schedule here are handled as follows: When the user first does a REST call to the API a session ID is returned as a header.
     * The user is already connected using socket.io, and will send a "identificationEvent" to identify the connection with the session.
     *
     * @param dispatcher
     */
    @Override
    protected void scheduleEndpoints(EndpointDispatcher dispatcher) {

        // get users
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetUsers(), "/users");

        //user creation
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new CreateUser(), "/users");

        //test show alex
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new Test(), "/hey/?", "name");

        //sign ups
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetUserSignups(), "/statistics/signups");

        //authentication API
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AuthenticationRequest(), "/authentication");

        // profile API /users/{userid}/profile

        // Interests API /users/{userid}/profile/interests/

        // search API {/search?q=xyz}

        // newsfeed {/users/{userid}/newsfeed}

        // trending {/users/{userid}/trending}

        // content API /content/
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AddNewContent(), "/content/?", "user_id");

        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new RequestContent(), "/content/");

        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new RequestContentTypes(), "/content/types");

        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AddContentType(), "/content/types");

        // following API /users/{userid}/following
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new UserFollowAction(), "/following/?", "user_id");

        // categories API /content/categories
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new RequestCategories(), "/content/categories");
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AddCategory(), "/content/categories");


        // saved content API {/users/{userid}/saved_content}

        // notifications API {/users/{userid}/notifications}

        // search API {/search?q=xyz}
    }

}
