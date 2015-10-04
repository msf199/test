package main.com.whitespell.peak.logic.endpoints;

import main.com.whitespell.peak.logic.EndpointDispatcher;
import main.com.whitespell.peak.logic.baseapi.WhitespellAPI;
import main.com.whitespell.peak.logic.endpoints.authentication.AuthenticationRequest;
import main.com.whitespell.peak.logic.endpoints.authentication.ExpireAuthentication;
import main.com.whitespell.peak.logic.endpoints.authentication.GetDeviceDetails;
import main.com.whitespell.peak.logic.endpoints.content.*;
import main.com.whitespell.peak.logic.endpoints.content.categories.AddCategory;
import main.com.whitespell.peak.logic.endpoints.content.categories.RequestCategories;
import main.com.whitespell.peak.logic.endpoints.content.types.AddContentType;
import main.com.whitespell.peak.logic.endpoints.content.types.AddReportingType;
import main.com.whitespell.peak.logic.endpoints.content.types.RequestContentTypes;
import main.com.whitespell.peak.logic.endpoints.content.types.RequestReportingTypes;
import main.com.whitespell.peak.logic.endpoints.monitoring.Ping;
import main.com.whitespell.peak.logic.endpoints.newsfeed.AddNewsfeed;
import main.com.whitespell.peak.logic.endpoints.newsfeed.GetEmptyNewsfeed;
import main.com.whitespell.peak.logic.endpoints.newsfeed.GetNewsfeed;
import main.com.whitespell.peak.logic.endpoints.statistics.GetUserSignups;
import main.com.whitespell.peak.logic.endpoints.users.*;
import main.com.whitespell.peak.logic.endpoints.users.publishers.GetUsersByCategory;
import main.com.whitespell.peak.logic.notifications.GetUserNotifications;

/**
 * @author Pim de Witte(wwadewitte) & Cory McAn(cmcan), Whitespell LLC
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

        // Simple ping reques that returns true.
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new Ping(), "/monitoring/ping");

        /**
         * USERS ENDPOINTS (TREE ORDER)
         */

        // get the list of users based on search criteria
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetUsers(), "/users");

        // add a new user (create account)
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new CreateUser(), "/users");

        // Get a specific user based on their user ID
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetUser(), "/users/$", "userId"); //always have the variable first

       // Get a specific user based on their user ID
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new ExpireAuthentication(), "/users/$/logout", "userId");

        // Get a specific user's workout based on their user ID
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetUserSavedContent(), "/users/$/saved", "userId");

        // Post a contentId to this endpoint to add to user's MyWorkouts
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AddToUserSavedContent(), "/users/$/saved", "userId");

        // Post a contentId to this endpoint to add to user's bundle
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AddToBundle(), "/users/$/bundles", "userId");

        // Get a specific user's saved list based on their user ID
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetBundle(), "/users/$/bundles", "userId");

		// As a user, update your username, displayname, and slogan
		dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new UpdateProfile(), "/users/$", "userId");

        // As a user, update your email or password
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new UpdateSettings(), "/users/$/settings", "userId");

		// get all the users sorted by categories (also takes in same search criteria as /users)
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetUsersByCategory(), "/users/categories/");

        // Add new content as a user
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AddNewContent(), "/users/$/content/", "userId");

        // Add new curated content as a user
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AddNewContentCuration(), "/users/$/contentcurated/", "userId");

        // Follow a user
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new UserFollowAction(), "/users/$/following", "userId");

        // Follow a category
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new CategoryFollowAction(), "/users/$/categories", "userId");

        // Publish in a category
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new CategoryPublishAction(), "/users/$/publishing", "userId");

        // Update user's email verification status (intended for use in web front end)
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new UpdateEmailVerification(), "/users/email");

        // Update user's email verification status (intended for use in web front end)
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new ResendEmailVerification(), "/users/resendemail");

        // Check user's email verification status
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetEmailVerification(), "/users/$/email", "userId");

        // Send out Forgot Password email
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new SendForgotPasswordEmail(), "/users/forgot");

        // Reset a user's password (Forgot Password)
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new ResetPassword(), "/users/reset");

        // Create a user account/link to current user account based on Facebook credentials
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new LinkFB(), "/users/facebook");

        // Check if the user's password is required based on the accessToken
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new CheckFBLinkStatus(), "/users/checkfacebook");

        // User sends feedback about Peak
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new SendFeedback(), "/users/$/feedback", "userId");

        // Get all of the reporting types for the ReportUser endpoint
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new RequestReportingTypes(), "users/reporting/types");

        // Add a reporting type to the database
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AddReportingType(), "users/reporting/types");

        // User reports another user for a specific reason
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new ReportUser(), "/users/$/reporting", "userId");

        //Get user device details
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetDeviceDetails(), "users/$/device", "userId");

        //Get user notifications
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetUserNotifications(), "users/$/notifications", "userId");

        //Update Email Notification status
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new UpdateEmailNotification(), "users/$/notifications", "userId");

        //Update a user's access to a content (such as when the user purchases a video)
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new GrantContentAccess(), "users/$/access", "userId");

        //Update a user's access to a content (such as when the user purchases a video)
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetTotalViews(), "users/$/views", "userId");

        /**
         * STATISTICS
         */

        // Get some sign up data
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetUserSignups(), "/statistics/signups");

        /**
         * AUTHENTICATION
         */

        //todo move auth under users
        //Authenticate a user
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AuthenticationRequest(), "/authentication");

        /**
         * CONTENT
         */

        // Get a list of all the content in the whole system based on certain search criteria
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new RequestContent(), "/content/");

        // Get a list of all the curated content in the whole system based on certain search criteria
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new RequestContentCuration(), "/contentcurated/");

        // Get a list of all the content types
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new RequestContentTypes(), "/content/types");

        // Add a new content type
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AddContentType(), "/content/types");

        // Get a content comment
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetContentComments(), "/content/$/comments", "contentId");

        // Add a content comment
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AddContentComment(), "/content/$/comments", "contentId");

        //Add/delete a content like
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new ContentLikeAction(), "/content/$/likes", "contentId");

        // add a child to a bundle
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AddContentToBundle(), "/content/$/add_child", "contentId");

        // Update content title, description and/or price
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new UpdateContent(), "/content/$", "contentId");

        // Update content title, description and/or price
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new ContentViewAction(), "/content/$/views", "contentId");

        /**
         * CATEGORIES
         */

        // Get a list of all the categories
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new RequestCategories(), "/categories");

        // Add a new category
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AddCategory(), "/categories");

        /**
         * SEARCH
         */

        // Get a search object
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new Search(), "/search");
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new Trending(), "/trending");

        /**
         * NEWSFEED
         */
        
        dispatcher.addHandler(EndpointDispatcher.RequestType.POST, new AddNewsfeed(), "/newsfeed/$", "userId" );
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetNewsfeed(), "/newsfeed/$", "userId");
        dispatcher.addHandler(EndpointDispatcher.RequestType.GET, new GetEmptyNewsfeed(), "/newsfeed/empty");
    }

}
