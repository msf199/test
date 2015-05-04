package whitespell.sample.MyApplication.endpoints.users.follow;

import org.eclipse.jetty.http.HttpStatus;
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
        context.getResponse().setStatus(HttpStatus.OK_200);
        context.getResponse().getWriter().write("{}");
        try {
            String posted_user_id = context.getUrlVariables().get("user_id");
            System.out.println(posted_user_id);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
