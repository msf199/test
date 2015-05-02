package whitespell.model;


public abstract class WhitespellIntelligence {

    protected final String apiKey;

    public WhitespellIntelligence(String apiKey) {
        this.apiKey = apiKey;
    }
    public abstract void start() throws Exception;

    public void addHandler(Action a) {
        System.out.println("Added handler interface: " + a.getActionName());
    }
}
