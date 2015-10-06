package main.com.whitespell.peak.logic;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.model.UserObject;

/**
 * @author Cory McAn(cmcan), Whitespell Inc.
 *         10/6/2015
 */
public class UserHelper {

    public UserObject getUserById(int userId) throws UnirestException {
        Gson g = new Gson();
        HttpResponse<String> stringResponse;

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/users/" + userId)
                .header("accept", "application/json")
                .header("X-Authentication", "-1," + StaticRules.MASTER_KEY + "")
                .asString();

        System.out.println(stringResponse.getBody());

        UserObject u = g.fromJson(stringResponse.getBody(), UserObject.class);

        if(stringResponse.getStatus() == 404){
            return null;
        }

        return u;
    }
}
