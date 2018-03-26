package cs455.hadoop.airline;

import cs455.hadoop.types.RecordData;
import cs455.hadoop.util.Airport;
import cs455.hadoop.util.Dictionary;
import cs455.hadoop.util.Plane;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Map;

public class MapTest extends Mapper<LongWritable, Text, Text, Text> {
    RecordData rec;
    IntWritable one;
    Dictionary dict;
    boolean once;

    @Override
    protected void setup(Mapper.Context context) throws IOException {
        Configuration conf = context.getConfiguration();
        dict = new Dictionary();
        dict.initialize(conf);
        rec = new RecordData(dict);
        one = new IntWritable(1);
        once = false;
    }

    @Override
    protected void map(LongWritable key, Text value, Mapper.Context context) throws IOException, InterruptedException {
        try {
            if (!once) {
                for (Map.Entry<String, String> c : dict.carriers.entrySet()) {
                    context.write(new Text(c.getKey()), new Text(c.getValue()));
                }
                for(Map.Entry<String, Airport> A : dict.airports.entrySet()) {
                    context.write(new Text(A.getKey()), new Text(String.format(
                            "%s, %s, %s, %s", A.getValue().getIata(), A.getValue().getCity(),
                            A.getValue().getAirport(), A.getValue().getState())));
                }
                for(Map.Entry<String, Plane> A : dict.planes.entrySet()) {
                    context.write(new Text(A.getKey()), new Text(String.format(
                            "%s, %s", A.getValue().getTailnum(),
                            A.getValue().isOld("2000"))));
                }
                once = true;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
