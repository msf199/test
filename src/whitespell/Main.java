package whitespell;

import whitespell.logic.UnitHandler;
import whitespell.logic.sql.Pool;
import whitespell.logic.sql.PoolTest;
import whitespell.model.Unit;
import whitespell.model.WhitespellIntelligence;
import whitespell.sample.MyApplication.MyEndpoints;
import whitespell.sample.MyApplication.MyIntelligence;
import whitespell.model.WhitespellWebServer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Main {

    public static void main(String[] args) throws Exception {

       // initialize MySQL Connection Pool
       Pool.initializePool();
       PoolTest.start(10);

        // start the API
        WhitespellWebServer testApi = new MyEndpoints("Peak API");
        System.out.println("Starting API on main thread.");
        testApi.startAPI(9001);

    }

}
