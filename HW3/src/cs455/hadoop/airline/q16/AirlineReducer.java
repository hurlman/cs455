package cs455.hadoop.airline.q16;

import cs455.hadoop.airline.KeyType;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class AirlineReducer extends Reducer<KeyType, IntWritable, KeyType, IntWritable> {
    @Override
    protected void reduce(KeyType key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        int totalDelay = 0;
        int numFlights = 0;
        for(IntWritable val : values){
            totalDelay += val.get();
            numFlights++;
        }
        int averageDelay = totalDelay / numFlights;
        context.write(key, new IntWritable(averageDelay));
    }
}
