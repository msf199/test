package tests.com.whitespell.peak;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         6/21/15
 *         tests.com.whitespell.peak
 */
public class HttpHandler {

    public static String returnGetRequest(String url) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");

        con.setRequestProperty("User-Agent", "Mozilla/5.0 ;Windows NT 6.1; WOW64; Trident/7.0; rv:11.0; like Gecko");

        //int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            if(inputLine.length() > 0) {
                response.append(inputLine);
            }
        }
        in.close();

        return response.toString();

    }

    public static boolean isOnline(String url) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");

        con.setRequestProperty("User-Agent", "Mozilla/5.0 ;Windows NT 6.1; WOW64; Trident/7.0; rv:11.0; like Gecko");

        int responseCode = con.getResponseCode();

        return responseCode == 200;

    }

}
