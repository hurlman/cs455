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

    public IntWritable getDepDelay(){
        return new IntWritable(Integer.parseInt(_record[15]));
    }
}
