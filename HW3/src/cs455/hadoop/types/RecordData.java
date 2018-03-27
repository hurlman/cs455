package cs455.hadoop.types;

import cs455.hadoop.util.Airport;
import cs455.hadoop.util.Dictionary;
import cs455.hadoop.util.Plane;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

public class RecordData {

    private String _record[];
    private int _depDelay;
    private Dictionary _dict;

    public RecordData(Dictionary dict, Text recordData) {
        _dict = dict;
        _record = recordData.toString().split("\\s*,\\s*");
        try {
            _depDelay = Integer.parseInt(_record[15]);
        } catch (NumberFormatException e) {
            _depDelay = 0;
        }
    }

    public KeyType getDepTime() {
        int time = Integer.parseInt(_record[5]);
        time -= time % 100;
        return new KeyType(FieldType.TIME_OF_DAY, String.format("%04d", time));
    }

    public String getCarrier() {
        return _dict.getCarrier(_record[8].toLowerCase());
    }

    public Airport getOriginAirport() {
        return _dict.getAirport(_record[16].toLowerCase());
    }

    public Airport getDestAirport() {
        return _dict.getAirport(_record[17].toLowerCase());
    }

    public Plane getPlane() {
        return _dict.getPlane(_record[10].toLowerCase());
    }

    public String getYear() {
        return _record[0];
    }

    public KeyType getDepDay() {
        int day = Integer.parseInt(_record[3]);
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
        int month = Integer.parseInt(_record[1]);
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

    public IntWritable getDepDelay() {
        return new IntWritable(_depDelay);
    }

    public boolean isDelayed() {
        return _depDelay > 0;
    }

    public boolean arrivedLate() {
        int i;
        try {
            i = Integer.parseInt(_record[14]);
        } catch (NumberFormatException e) {
            i = 0;
        }
        return i > 0;
    }

    public boolean hasWeatherDelay() {
        int i;
        try {
            i = Integer.parseInt(_record[25]);
        } catch (NumberFormatException e) {
            i = 0;
        }
        return i > 0;
    }
}
