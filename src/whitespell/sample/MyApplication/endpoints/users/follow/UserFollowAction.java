package whitespell.sample.MyApplication.endpoints.users.follow;

import whitespell.logic.ApiInterface;
import whitespell.logic.RequestContext;

import java.io.IOException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class UserFollowAction implements ApiInterface {

    public void call(RequestContext context) throws IOException {
        String testId = context.getUrlVariables().get("test_id");
        context.getResponse().getWriter().write("Test:" + testId);
    }

    /**
     * HandleDistribution is the class that the user fills in to route the correct requests to the correct personalized ads.
     * @param context               The request context
     * @param session               the {@link whitespell.model.baseapi.Session}
     */
}
