package main.com.whitespell.peak.logic.endpoints.newsfeed;

import com.google.gson.Gson;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.Authentication;
import main.com.whitespell.peak.logic.EndpointHandler;
import main.com.whitespell.peak.logic.RequestObject;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.sql.ExecutionBlock;
import main.com.whitespell.peak.logic.sql.StatementExecutor;
import main.com.whitespell.peak.model.UserObject;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by cory on 15/07/15.
 */
public class NewsFeed extends EndpointHandler {

    private static final String QS_FOLLOWERS_KEY = "includeFollowing";

    @Override
    public void setUserInputs(){

    }

    @Override
    public void safeCall(final RequestObject context) throws IOException {

    }
}

