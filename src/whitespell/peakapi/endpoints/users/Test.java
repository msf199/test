package whitespell.peakapi.endpoints.users;

import whitespell.logic.EndpointInterface;
import whitespell.logic.RequestContext;

import java.io.IOException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class Test implements EndpointInterface {

    @Override
    public void call(RequestContext context) throws IOException {

        /*Enumeration e = (context.getRequest().getHeaderNames());

        while(e.hasMoreElements()) {
            String param = (String) e.nextElement();
            System.out.println(param + "->" + context.getRequest().getHeader(param));
        }*/

       context.getResponse().getWriter().write("Hey, I heard your name is:" + context.getUrlVariables().get("name"));


    }

}
