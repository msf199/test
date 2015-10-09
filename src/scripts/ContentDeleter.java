package scripts;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.ContentHelper;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.model.ContentObject;

/**
 * @author Cory McAn(cmcan), Whitespell LLC
 *         10/8/15
 *         whitespell.logic.endpoints.users
 */

public class ContentDeleter {

    public static void main(String args[]){
        /**
         * Used to delete junk content from db along with thumbnails and videos if they exist
         */

        int firstContentId = 14061;
        int lastContentId = 14130;

        ContentHelper h = new ContentHelper();
        ContentObject toDelete;

        for(int i = firstContentId; i<= lastContentId; i++){
            try {
                toDelete = h.getContentById(i);

                HttpResponse<String> stringResponse = Unirest.delete("https://peakapi.whitespell.com/content/" + i)
                        .header("accept", "application/json")
                        .header("X-Authentication", "-1," + StaticRules.MASTER_KEY + "")
                        .asString();
                System.out.println(stringResponse.getBody());
            }catch(Exception e){
                Logging.log("Low", e);
            }
        }
    }
}
