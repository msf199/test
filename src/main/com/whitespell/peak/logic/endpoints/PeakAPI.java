package main.com.whitespell.peak.logic.endpoints;

import main.com.whitespell.peak.logic.EndpointDispatcher;
import main.com.whitespell.peak.logic.baseapi.WhitespellAPI;
import main.com.whitespell.peak.logic.endpoints.content.AddNewContent;
import main.com.whitespell.peak.logic.endpoints.content.RequestContent;
import main.com.whitespell.peak.logic.endpoints.content.categories.AddCategory;
import main.com.whitespell.peak.logic.endpoints.content.categories.RequestCategories;
import main.com.whitespell.peak.logic.endpoints.content.types.AddContentType;
import main.com.whitespell.peak.logic.endpoints.content.types.RequestContentTypes;
import main.com.whitespell.peak.logic.endpoints.ping.Ping;
import main.com.whitespell.peak.logic.endpoints.statistics.GetUserSignups;
import main.com.whitespell.peak.logic.endpoints.users.*;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/4/15
 *         whitespell.endpoints
 */
public class PeakAPI extends WhitespellAPI {

    /**
     * MyEndpoints schedules the endpoints in the API.
     *
     * @param dispatcher
     */

    @Override
    protected void scheduleEndpoints(EndpointDispatcher dispatcher) {

        /**
         * PING
         */
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new Ping(), "/ping");

        /**
         * USERS ENDPOINTS (TREE ORDER)
         */

        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetUsers(), "/users");
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new CreateUser(), "/users");
        ;
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetUser(), "/users/?", "user_id");
        ;
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new UserFollowAction(), "/users/?/following", "user_id");
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new CategoryFollowAction(), "/users/?/categories", "user_id");

        /**
         * STATISTICS
         */

        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetUserSignups(), "/statistics/signups");

        /**
         * AUTHENTICATION
         */

        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AuthenticationRequest(), "/authentication");


        /**
         * CONTENT
         */

        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new RequestContent(), "/content/");
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AddNewContent(), "/content/?", "user_id");
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new RequestContentTypes(), "/content/types");
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AddContentType(), "/content/types");
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new RequestCategories(), "/content/categories");
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AddCategory(), "/content/categories");

    }

}
