package whitespell.model;

import whitespell.logic.ApiInterface;
import whitespell.logic.RequestContext;
import whitespell.logic.SessionHandler;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public abstract class UnitEndpoint implements ApiInterface {

    public void call(RequestContext context) {
        String adUUID = context.getUrlVariables().get("ad_id");
        Session session = SessionHandler.getSession(context.getResponse().getHeader("Session"));
        session.putPersonalizedAd(context.getResponse().getHeader("Request-id"), adUUID);
        Unit ad = session.getAd(adUUID);
    }

    /**
     * HandleDistribution is the class that the user fills in to route the correct requests to the correct personalized ads.
     * @param context               The request context
     * @param ad                    the {@link Unit}
     * @param session               the {@link whitespell.model.Session}
     */

    public abstract void handleDistribution(RequestContext context, Unit ad, Session session);

}
