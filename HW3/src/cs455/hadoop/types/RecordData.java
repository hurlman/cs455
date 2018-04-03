package cs455.hadoop.types;

import cs455.hadoop.util.Airport;
import cs455.hadoop.util.Dictionary;
import cs455.hadoop.util.Plane;
import org.apache.hadoop.io.Text;

/**
 * Class that parses and represents each line of input data in the map phase.  Provides
 * public methods for the mapper to retrieve data about the record such as whether the flight was
 * delayed, how long the delay was, what plane flew, and what airports were the origin and
 * destination among other things.
 *
 * Uses the dictionary class to return the full information on the plane and airports.
 */
public class RecordData {

    private String record[];
    private int depDelay;
    private int distance;
    private Dictionary dict;

    public RecordData(Dictionary dict, Text recordData) {
        this.dict = dict;
        record = recordData.toString().split("\\s*,\\s*");
        try {
            depDelay = Integer.parseInt(record[15]);
        } catch (NumberFormatException e) {
            depDelay = 0;
        }
        try {
            distance = Integer.parseInt(record[18]);
        } catch (NumberFormatException e) {
            distance = 0;
        }
    }

    public KeyType getDepTime() {
        int time = Integer.parseInt(record[5]);
        time -= time % 100;
        if (time >= 2400) time = 0;
        return new KeyType(FieldType.TIME_OF_DAY, String.format("%04d", time));
    }

    public String getCarrier() {
        return dict.getCarrier(record[8].toLowerCase());
    }

    public Airport getOriginAirport() {
        return dict.getAirport(record[16].toLowerCase());
    }

    public Airport getDestAirport() {
        return dict.getAirport(record[17].toLowerCase());
    }

    public Plane getPlane() {
        return dict.getPlane(record[10].toLowerCase());
    }

    public String getYear() {
        return record[0];
    }

    public int getDistance() {
        return distance;
    }

    public KeyType getDepDay() {
        int day = Integer.parseInt(record[3]);
        String sDay = "";
        switch (day) {
            case 1:
                sDay = "Monday";
                break;
            case 2:
                sDay = "Tuesday";
                break;
            case 3:
                sDay = "Wednesday";
                break;
            case 4:
                sDay = "Thursday";
                break;
            case 5:
                sDay = "Friday";
                break;
            case 6:
                sDay = "Saturday";
                break;
            case 7:
                sDay = "Sunday";
                break;
        }
        return new KeyType(FieldType.DAY_OF_WEEK, sDay);
    }

    public KeyType getDepMonth() {
        int month = Integer.parseInt(record[1]);
        String sMonth = "";
        switch (month) {
            case 1:
                sMonth = "January";
                break;
            case 2:
                sMonth = "February";
                break;
            case 3:
                sMonth = "March";
                break;
            case 4:
                sMonth = "April";
                break;
            case 5:
                sMonth = "May";
                break;
            case 6:
                sMonth = "June";
                break;
            case 7:
                sMonth = "July";
                break;
            case 8:
                sMonth = "August";
                break;
            case 9:
                sMonth = "September";
                break;
            case 10:
                sMonth = "October";
                break;
            case 11:
                sMonth = "November";
                break;
            case 12:
                sMonth = "December";
        }
        return new KeyType(FieldType.MONTH_OF_YEAR, sMonth);

    }

    public int getDepDelay() {
        return depDelay;
    }

    public boolean isDelayed() {
        return depDelay > 0;
    }

    public boolean arrivedLate() {
        int i;
        try {
            i = Integer.parseInt(record[14]);
        } catch (NumberFormatException e) {
            i = 0;
        }
        return i > 0;
    }

    public boolean hasWeatherDelay() {
        int i;
        try {
            i = Integer.parseInt(record[25]);
        } catch (NumberFormatException e) {
            i = 0;
        }
        return i > 0;
    }
}
