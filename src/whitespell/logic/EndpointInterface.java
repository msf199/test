package whitespell.logic;

import java.io.IOException;

public interface EndpointInterface {
    public abstract void call(RequestContext context) throws IOException;
}
