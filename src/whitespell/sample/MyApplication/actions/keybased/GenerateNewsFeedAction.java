package whitespell.sample.MyApplication.actions.keybased;

import whitespell.model.Action;
import whitespell.model.ActionType;
import whitespell.sample.MyApplication.MyIntelligence;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/4/15
 *         whitespell.sample.MyApplication.actions
 */
public class GenerateNewsFeedAction extends Action {

    public GenerateNewsFeedAction() {
        super("Generate Newsfeed", ActionType.Java, new String[]{
                "$userid"
        });
    }

    @Override
    protected void execute() {

        /*Session session = MyIntelligence.getSession(this.getSessionId());
        // generate newsfeed

        String newsfeed = "[" +
                "{'post_id': '9870097', 'owner' : 'random_user_id', 'profile_photo' : 'http://cdn.yourdomain.com/profile_photos/my_photo-randomstring.jpg'}" +
                "]";

        String userId = (String) this.getVariables().get("$userid");

        SmartCacheJsonArray sco = MyIntelligence.getSessions().get("/users/" + userid + "/posts");

        if(sco == null) {
            sco.setCachingLimit(100); // this is the amount it will also return on a request with empty parameters,
            /**
             * If the user does a request that for example requests a range larger than these 100 objects, a request is sent to  the back-end and only the difference is served.
             */
           /* sco.addIndex("post_id");
            sco.setLimitingParameter("limit");
            sco.setOffsetParameter("offset");
            sco.setMinParameter("min_post_id");
            sco.setMaxParameter("max_post_id");
        }
        sco.putContent(newsfeed);
        session.addCachableObject(sco);
*/
    }

    @Override
    protected boolean doForceExecute() {
        return false;
    }

    @Override
    protected long forceExecuteCheckTime() {
        return 0;
    }

    @Override
    protected long cacheTime() {
        return 0;
    }
}
