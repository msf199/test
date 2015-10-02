package main.com.whitespell.peak.logic;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.model.ContentObject;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         10/2/15
 *         main.com.whitespell.peak.logic
 */
public class ContentHelper {

    public ContentObject getContentById(int contentId) throws UnirestException {
        ContentObject temptObject;

        Gson g = new Gson();
        HttpResponse<String> stringResponse;

        stringResponse = Unirest.get("http://localhost:" + Config.API_PORT + "/content/?contentId="+contentId)
                .header("accept", "application/json")
                .header("X-Authentication", "-1," + StaticRules.MASTER_KEY + "")
                .asString();

        ContentObject c[] = g.fromJson(stringResponse.getBody(), ContentObject[].class);

        return c[0];
    }

}
