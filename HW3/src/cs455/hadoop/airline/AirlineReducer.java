package cs455.hadoop.airline;

import cs455.hadoop.types.FieldType;
import cs455.hadoop.types.KeyType;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class AirlineReducer extends Reducer<KeyType, IntWritable, KeyType, IntWritable> {
    @Override
    protected void reduce(KeyType key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        switch (FieldType.getFieldType(key.getCategory())) {
            case TIME_OF_DAY:
            case DAY_OF_WEEK:
            case MONTH_OF_YEAR:
            case CARRIER_AVG:
            case PLANE:
                WriteAverage(key, values, context);
                break;
            case AIRPORT:
            case CARRIER_TOT:
            case WEATHER_CITY:
                WriteTotal(key, values, context);
                break;
        }
    }

    private void WriteTotal(KeyType key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        int total = 0;
        for(IntWritable val : values){
            total += val.get();
        }
        context.write(key, new IntWritable(total));
    }

    private void WriteAverage(KeyType key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        int totalDelay = 0;
        int numFlights = 0;
        for (IntWritable val : values) {
            totalDelay += val.get();
            numFlights++;
        }
        int averageDelay = totalDelay / numFlights;
        context.write(key, new IntWritable(averageDelay));
    }
}
