package whitespell.logic;

import java.io.IOException;

public interface ApiInterface {
    public abstract void call(RequestContext context) throws IOException;
}
