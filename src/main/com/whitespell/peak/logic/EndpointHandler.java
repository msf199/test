package main.com.whitespell.peak.logic;

import main.com.whitespell.peak.StaticRules;

import java.io.IOException;
import java.util.HashMap;

public abstract class EndpointHandler {

    public EndpointHandler() {
        this.setUserInputs();
    }

    public HashMap<String, StaticRules.InputTypes> getUrlInput() {
        return urlInput;
    }

    public HashMap<String, StaticRules.InputTypes> getQueryStringInput() {
        return queryStringInput;
    }

    public HashMap<String, StaticRules.InputTypes> getPayloadInput() {
        return payloadInput;
    }

    public HashMap<String, StaticRules.InputTypes> payloadInput = new HashMap<>();
    public HashMap<String, StaticRules.InputTypes> urlInput = new HashMap<>();
    public HashMap<String, StaticRules.InputTypes> queryStringInput = new HashMap<>();


    public abstract void safeCall(RequestObject context) throws IOException;
    protected abstract void setUserInputs();
}
