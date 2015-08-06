package main.com.whitespell.peak.model;

import main.com.whitespell.peak.logic.logging.Logging;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author  Cory McAn for Whitespell
 *          8/6/2015
 *          whitespell.model
 */
public class DateObject implements Comparable<Object>{

    private Date date;
    private Time time;

    public DateObject(Date date, Time time) {
        this.date = date;
        this.time = time;
    }

    public int compareTo(Object o) {

        DateFormat formatter;
        Date date1 = null;
        Date date2 = null;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date1 = formatter.parse(this.date + " " + this.time);
            date2 = formatter.parse(this.date + " " + this.time);
        } catch (ParseException e) {
            Logging.log("High", e);
        } catch (Exception e){
            Logging.log("High", e);
        }

        return date1.compareTo(date2);
    }

    @Override
    public String toString(){
        return this.date+" "+this.time;
    }
}
