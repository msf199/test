package main.com.whitespell.peak.logic.endpoints.users;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.ContentObject;
import main.com.whitespell.peak.model.UserObject;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Pim de Witte(wwadewitte) & Cory McAn(cmcan), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public class Search extends EndpointHandler {

    @Override
    protected void setUserInputs() {

    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

        SearchResponse search = new SearchResponse();


        Gson g = new Gson();
        String response = g.toJson(search);
        context.getResponse().setStatus(200);
        try {
            context.getResponse().getWriter().write(response);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }


    public class SearchResponse {


        public SearchResponse() {
            profiles = new ArrayList<>();
            categories = new ArrayList<>();
            performances = new ArrayList<>();

            double rand =  Math.random() * 100;
            if(rand <= 25) {
                profiles.add(new UserObject(
                134, "pimdewitte", "Pim de Witte", "pimdewitte95@gmail.com", "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-xaf1/v/t1.0-1/p480x480/11080893_10205223324717138_6070570333018039963_n.jpg?oh=661c4c48564883235b97fae2ea1a8da0&oe=560FBE53&__gda__=1443851720_8e1084da2ce8668b2b23797a71538c61", "https://scontent-mia1-1.xx.fbcdn.net/hphotos-xpa1/t31.0-8/10494321_10204560004254541_1081362915244825598_o.jpg", "eat food and get fat"
                ));
                profiles.add(new UserObject(
                        135, "pimdewitte2", "Prim de Zitte", "pimdewitte96@gmail.com", "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-xaf1/v/t1.0-1/p480x480/11080893_10205223324717138_6070570333018039963_n.jpg?oh=661c4c48564883235b97fae2ea1a8da0&oe=560FBE53&__gda__=1443851720_8e1084da2ce8668b2b23797a71538c61", "https://scontent-mia1-1.xx.fbcdn.net/hphotos-xpa1/t31.0-8/10494321_10204560004254541_1081362915244825598_o.jpg", "eat food and get fatter"
                ));
                profiles.add(new UserObject(
                        136, "pimdewitte3", "Zim de Wite", "pimdewitte97@gmail.com", "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-xaf1/v/t1.0-1/p480x480/11080893_10205223324717138_6070570333018039963_n.jpg?oh=661c4c48564883235b97fae2ea1a8da0&oe=560FBE53&__gda__=1443851720_8e1084da2ce8668b2b23797a71538c61", "https://scontent-mia1-1.xx.fbcdn.net/hphotos-xpa1/t31.0-8/10494321_10204560004254541_1081362915244825598_o.jpg", "eat food and get even fatter"
                ));

                categories.add(1);
                categories.add(3);


                performances.add(new ContentObject(
                1, "Content Object 1",
                       "https://www.youtube.com/watch?v=fcN37TxBE_s",
                        "Test Workout"
                ));

                performances.add(new ContentObject(
                        2, "Content Object 2",
                        "https://www.youtube.com/watch?v=fcN37TxBE_s",
                        "Test Workout"
                ));

                performances.add(new ContentObject(
                        3, "Content Object 3",
                        "https://www.youtube.com/watch?v=fcN37TxBE_s",
                        "Test Workout"
                ));
            } else if( rand > 25 && rand <= 50) {

                performances.add(new ContentObject(
                        1, "Content Object 1",
                        "https://www.youtube.com/watch?v=fcN37TxBE_s",
                        "Test Workout"
                ));

                performances.add(new ContentObject(
                        8, "Content Object 8",
                        "https://www.youtube.com/watch?v=fcN37TxBE_s",
                        "Test Workout"
                ));

                performances.add(new ContentObject(
                        12, "Content Object 12",
                        "https://www.youtube.com/watch?v=fcN37TxBE_s",
                        "Test Workout"
                ));


                profiles.add(new UserObject(
                        100, "pimdewitte", "Wow de Witte", "pimdewi5@gmail.com", "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-xaf1/v/t1.0-1/p480x480/11080893_10205223324717138_6070570333018039963_n.jpg?oh=661c4c48564883235b97fae2ea1a8da0&oe=560FBE53&__gda__=1443851720_8e1084da2ce8668b2b23797a71538c61", "https://scontent-mia1-1.xx.fbcdn.net/hphotos-xpa1/t31.0-8/10494321_10204560004254541_1081362915244825598_o.jpg", "eat food and get fat"
                ));
                profiles.add(new UserObject(
                        101, "pimdewitte2", "Haha de Zitte", "pimdewitte@gmail.com", "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-xaf1/v/t1.0-1/p480x480/11080893_10205223324717138_6070570333018039963_n.jpg?oh=661c4c48564883235b97fae2ea1a8da0&oe=560FBE53&__gda__=1443851720_8e1084da2ce8668b2b23797a71538c61", "https://scontent-mia1-1.xx.fbcdn.net/hphotos-xpa1/t31.0-8/10494321_10204560004254541_1081362915244825598_o.jpg", "eat food and get fatter"
                ));
                profiles.add(new UserObject(
                        102, "pimdewitte3", "Noob de Wite", "pimdewitt@gmail.com", "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-xaf1/v/t1.0-1/p480x480/11080893_10205223324717138_6070570333018039963_n.jpg?oh=661c4c48564883235b97fae2ea1a8da0&oe=560FBE53&__gda__=1443851720_8e1084da2ce8668b2b23797a71538c61", "https://scontent-mia1-1.xx.fbcdn.net/hphotos-xpa1/t31.0-8/10494321_10204560004254541_1081362915244825598_o.jpg", "eat food and get even fatter"
                ));

                categories.add(2);
                categories.add(4);
            } else {

                performances.add(new ContentObject(
                        11, "Content Object 11",
                        "https://www.youtube.com/watch?v=fcN37TxBE_s",
                        "Test Workout"
                ));

                performances.add(new ContentObject(
                        18, "Content Object 18",
                        "https://www.youtube.com/watch?v=fcN37TxBE_s",
                        "Test Workout"
                ));

                performances.add(new ContentObject(
                        19, "Content Object 19",
                        "https://www.youtube.com/watch?v=fcN37TxBE_s",
                        "Test Workout"
                ));

                profiles.add(new UserObject(
                        111, "aaaa", "Ho**444$$ooo de Witte", "pimdewitte95@gmail.com", "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-xaf1/v/t1.0-1/p480x480/11080893_10205223324717138_6070570333018039963_n.jpg?oh=661c4c48564883235b97fae2ea1a8da0&oe=560FBE53&__gda__=1443851720_8e1084da2ce8668b2b23797a71538c61", "https://scontent-mia1-1.xx.fbcdn.net/hphotos-xpa1/t31.0-8/10494321_10204560004254541_1081362915244825598_o.jpg", "eat food and get fat"
                ));
                profiles.add(new UserObject(
                        112, "ddd", "BBBBB de Wite", "pimdewitt@gmail.com", "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-xaf1/v/t1.0-1/p480x480/11080893_10205223324717138_6070570333018039963_n.jpg?oh=661c4c48564883235b97fae2ea1a8da0&oe=560FBE53&__gda__=1443851720_8e1084da2ce8668b2b23797a71538c61", "https://scontent-mia1-1.xx.fbcdn.net/hphotos-xpa1/t31.0-8/10494321_10204560004254541_1081362915244825598_o.jpg", "eat food and get even fatter"
                ));

                categories.add(1);
                categories.add(2);
                categories.add(3);
                categories.add(4);

            }
        }
        public ArrayList<UserObject> profiles;
        public ArrayList<Integer> categories;
        public ArrayList<ContentObject> performances;
    }
}
