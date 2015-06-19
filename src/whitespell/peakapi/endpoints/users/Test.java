package whitespell.peakapi.endpoints.users;

import whitespell.logic.EndpointInterface;
import whitespell.logic.RequestObject;

import java.io.IOException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class Test implements EndpointInterface {

    @Override
    public void call(RequestObject context) throws IOException {

        /*Enumeration e = (context.getRequest().getHeaderNames());

        while(e.hasMoreElements()) {
            String param = (String) e.nextElement();
            System.out.println(param + "->" + context.getRequest().getHeader(param));
        }*/

       context.getResponse().getWriter().write("Hey, hello:" + context.getUrlVariables().get("name"));


    }

}
