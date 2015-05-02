package whitespell;

import whitespell.logic.UnitHandler;
import whitespell.model.Unit;
import whitespell.model.WhitespellIntelligence;
import whitespell.sample.MyApplication.MyEndpoints;
import whitespell.sample.MyApplication.MyIntelligence;
import whitespell.model.WhitespellWebServer;

public class Main {

    public static void main(String[] args) throws Exception {
       WhitespellWebServer testApi = new MyEndpoints("test");
       testApi.startAPI(9001);
       System.out.println("API Started");
       testApi.startWebsockets();
    }

}
