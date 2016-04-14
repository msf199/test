package main.com.whitespell.peak;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.StatementExecutor;

import java.sql.SQLException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         12/10/15
 *         main.com.whitespell.peak
 */
public class TestAppleOrder {

    static String fml = "";

    public static void main(String[] args) {
        HttpResponse<String> stringResponse = null;
        try {
            stringResponse = Unirest.post("https://sandbox.itunes.apple.com/verifyReceipt")
                    .header("accept", "application/json")
                    .body("{\n" +
                            "\"receipt-data\":" + "\"" +fml+ "\"" +
                            "}")
                    .asString();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        if(stringResponse.getBody() != null && stringResponse.getBody().contains("\"status\":0")) {
            JsonParser parser = new JsonParser();
            JsonObject o = parser.parse(stringResponse.getBody()).getAsJsonObject();
            JsonArray inApp = o.get("receipt").getAsJsonObject().get("in_app").getAsJsonArray();

            for (int i = 0; i < inApp.size(); i++) {
                if (i == inApp.size() - 1) {
                    String orderUUID = inApp.get(i).getAsJsonObject().get("transaction_id").getAsString();
                    System.out.println(orderUUID);
                }

            }
        } else {
            // throw error
        }


            }
}
