package whitespell.peakapi.endpoints.users;

import whitespell.logic.EndpointInterface;
import whitespell.logic.RequestObject;

import java.io.IOException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class UserHandler implements EndpointInterface {

    public void call(RequestObject context) throws IOException {
        String testId = context.getUrlVariables().get("test_id");
        context.getResponse().getWriter().write("Test:" + testId);
    }

    /**
     * HandleDistribution is the class that the user fills in to route the correct requests to the correct personalized ads.
     * @param context               The request context
     * @param session               the {@link whitespell.model.baseapi.Session}
     */
}
