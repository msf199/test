package whitespell.model.baseapi;


public abstract class WhitespellIntelligence {

    protected final String apiKey;

    public WhitespellIntelligence(String apiKey) {
        this.apiKey = apiKey;
    }
    public abstract void start() throws Exception;

}
