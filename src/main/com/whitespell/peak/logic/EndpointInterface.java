package main.com.whitespell.peak.logic;

import java.io.IOException;

public interface EndpointInterface {
    public abstract void call(RequestObject context) throws IOException;
}
