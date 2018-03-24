package cs455.hadoop.airline;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

public class RecordData {

    private String _record[];

    public RecordData(Text recordData ){
        _record = recordData.toString().split("\\s*,\\s*");
    }

    public KeyType getDepTime(){
        int time = Integer.parseInt(_record[5]);
        time -= time % 100;
        return new KeyType(FieldType.TIME_OF_DAY, String.format("%04d", time));
    }

    public KeyType getDepDay(){
        int day = Integer.parseInt(_record[3]);
        String sDay = "";
        switch (day){
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

    public KeyType getDepMonth(){
        int month = Integer.parseInt(_record[1]);
        String sMonth = "";
        switch (month){
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

    public IntWritable getDepDelay(){
        int i;
        try{
            i = Integer.parseInt(_record[15]);
        }catch (NumberFormatException e){
            i = 0;
        }
        return new IntWritable(i);
    }
}
