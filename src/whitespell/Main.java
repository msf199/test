package whitespell;

import whitespell.logic.sql.Pool;
import whitespell.logic.sql.PoolTest;
import whitespell.sample.MyApplication.MyEndpoints;
import whitespell.model.baseapi.WhitespellWebServer;

public class Main {

    public static void main(String[] args) throws Exception {

       // initialize MySQL Connection Pool
       Pool.initializePool();

        // start the API
        WhitespellWebServer testApi = new MyEndpoints("Peak API");
        System.out.println("Starting API on main thread.");
        testApi.startAPI(9001);

    }

}
