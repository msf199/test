package whitespell.sample.MyApplication.endpoints.users.follow;

import com.google.gson.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import whitespell.StaticRules;
import whitespell.logic.ApiInterface;
import whitespell.logic.RequestContext;
import whitespell.logic.Safety;

import java.io.IOException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class UserFollowAction implements ApiInterface {

    private static final String FOLLOWING_USER_ID_KEY = "following_id";
    private static final String ACTION_KEY = "action";

    public void call(RequestContext context) throws IOException {
        String posted_user_id = context.getUrlVariables().get("user_id");

        JsonObject payload = context.getPayload().getAsJsonObject();

        if (!Safety.isNumeric(posted_user_id) || payload.get(FOLLOWING_USER_ID_KEY) == null || payload.get(ACTION_KEY) == null) {
            context.throwHttpError(StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }

        int user_id = Integer.parseInt(posted_user_id);
        int following_user_id = payload.get(FOLLOWING_USER_ID_KEY).getAsInt();
        String action = payload.get(ACTION_KEY).getAsString();

        boolean validAction = action.equalsIgnoreCase("follow") || action.equalsIgnoreCase("unfollow");

        if (!validAction) {
            context.throwHttpError(StaticRules.ErrorCodes.NULL_VALUE_FOUND);
            return;
        }

        System.out.println("user_id: " + user_id);
        System.out.println("following_id: " + following_user_id);
        System.out.println("action: " + action);

        context.getResponse().setStatus(HttpStatus.OK_200);
        context.getResponse().getWriter().write("{}");
    }

}
