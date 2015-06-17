package whitespell.model.baseapi;

import whitespell.logic.EndpointInterface;
import whitespell.logic.RequestContext;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         1/20/15
 *         whitespell.model
 */
public abstract class Endpoint implements EndpointInterface {

    public abstract void call(RequestContext context);

}
