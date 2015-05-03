package whitespell.model.baseapi;

import whitespell.logic.RequestContext;

import java.util.HashMap;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public abstract class Action {

    /**
     * The Action object is an object that gets executed by an event, endpoint, or incoming action packet from an application-authorized server or Whitespell server.
     * This model stores which dependencies an action depends on before being executed
     * Example: Dependencies is group_id. In the login script saves the variable group_id and this action fires off.
     * Actions should NEVER depend on any sort of user input. Input has to be validated by endpoints and not by actions.
     * Actions are stored in the {@link Application} model
     * Actions are stateless, and should be scalable. The {@link whitespell.logic.FrostwaveClient} notifies the action if the variables comes available
     * @param actionName            The name of the action, mainly used for logging purposes and reverse-lookup
     * @param actionType            The type of the action
     * @param variables                  The variables that lead to this action being executed
     * @returns Action
     * @more https://docs.google.com/a/whitespell.com/document/d/1mVESYT38PDHDgMbiaJTMhrUZ5xHlUIGvpBSf-hXSaio/
     */

    private final String actionName;

    /**
     * The RequestContext object is only set when a direct request is made through HTTP.
     */

    private RequestContext context;


    private final ActionType actionType;

    /**
     * The session ID is provided when the action is requested from the intelligence. The reason for this is that the variables that are unlocked are only verified for this session iD.
     * Therefore the cache can for example only cache the newsfeed for user x as a static object because this session can only be user x.
     */
    private String sessionId;

    /**
     * The variables that are required for the action to execute and their values
     */

    private final HashMap<String, Object> variables = new HashMap<>();

    public Action(String actionName, ActionType actionType, String[] variables) {
        this.actionName = actionName;
        this.actionType = actionType;

        for(String variable : variables) {
            this.variables.put(variable, null);
        }

    }

    /**
     * The execute function is called when the action needs execution. The execute function is basically the body of the request.
     */

    protected abstract void execute();


    /**
     * This function is called every checkReExecuteTimer() milliseconds. If this returns true, the action will be regenerated
     * and the users that are subscribed to this action will be re-notified.
     */
    protected abstract boolean doForceExecute();

    /**
     * The time in milliseconds the action is re-executed
     * @return
     */
    protected abstract long forceExecuteCheckTime();

    /**
     * The tie in milliseconds that this action can be cached for
     * @return
     */
    protected abstract long cacheTime();

    /**
     * Returns the list of variables and their values
     */
    protected HashMap<String, Object> getVariables() {
        return this.variables;
    }

    protected String getActionName(){
        return this.actionName;
    }

    protected ActionType getActionType(){
        return this.actionType;
    }

    protected String getSessionId(){
        return this.sessionId;
    }

    protected RequestContext getContext() {
        return this.context;
    }

}
