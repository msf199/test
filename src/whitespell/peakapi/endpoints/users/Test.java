package whitespell.peakapi.endpoints.users;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import whitespell.StaticRules;
import whitespell.logic.ApiInterface;
import whitespell.logic.RequestContext;
import whitespell.logic.SessionIdentifierGenerator;
import whitespell.logic.sql.Pool;
import whitespell.model.AuthenticationObject;
import whitespell.security.PasswordHash;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class Test implements ApiInterface {

    @Override
    public void call(RequestContext context) throws IOException {

        Enumeration e = (context.getRequest().getHeaderNames());

        while(e.hasMoreElements()) {
            String param = (String) e.nextElement();
            System.out.println(param + "->" + context.getRequest().getHeader(param));
        }

       context.getResponse().getWriter().write("Test for Galo with Galo ID:" + context.getUrlVariables().get("userid"));


    }

}
