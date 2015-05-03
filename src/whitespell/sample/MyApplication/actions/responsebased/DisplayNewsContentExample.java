package whitespell.sample.MyApplication.actions.responsebased;

import whitespell.model.baseapi.Action;
import whitespell.model.baseapi.ActionType;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/4/15
 *         whitespell.sample.MyApplication.actions
 */
public class DisplayNewsContentExample extends Action {

    public DisplayNewsContentExample() {
        super("Generate Newsfeed", ActionType.Java, new String[]{
                "$userid"
        });
    }

    @Override
    protected void execute() {
        /*
        Session session = MyIntelligence.getSession(this.getSessionId());
        // generate newsfeed

        String newsfeed = "[" +
                "{'post_id': '9870097', 'owner' : 'random_user_id', 'profile_photo' : 'http://cdn.yourdomain.com/profile_photos/my_photo-randomstring.jpg'}" +
                "]";

        SmartCacheJsonArray sco = MyIntelligence.getSessions().set("/users/");
        sco.setCachingLimit(100);
        sco.addIndex("post_id");

        sco.setLimitingParameter("limit");
        sco.setOffsetParameter("offset");

        sco.setMinParameter("min_post_id");
        sco.setMaxParameter("max_post_id");


        MyIntelligence.updateCache(this.getSessionId(), "/users/" + this.getVariables().get("$userid") + "/newsfeed");
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
