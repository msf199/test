package main.com.whitespell.peak.logic.sql;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         7/10/15
 *         main.com.whitespell.peak.logic.sql
 */
public class ExtraSqlThread implements Runnable {

    StatementExecutor statement;

    @Override
    public void run() {

    }

    public void run(StatementExecutor s){
        this.statement = s;
        this.run();
    }
}
