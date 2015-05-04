package whitespell.sample.MyApplication.endpoints.users.content;

import org.eclipse.jetty.http.HttpStatus;
import whitespell.logic.ApiInterface;
import whitespell.logic.RequestContext;

import java.io.IOException;

/**
 * @author Josh Lipson(mrgalkon)
 * 5/4/2015
 */
public class AddNewContent implements ApiInterface {


    public void call(RequestContext context) throws IOException {
        context.getResponse().setStatus(HttpStatus.OK_200);
        context.getResponse().getWriter().write("{}");
    }

}
