package cs455.hadoop.airline;

import cs455.hadoop.types.FieldType;
import cs455.hadoop.types.IntPair;
import cs455.hadoop.types.KeyType;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class AirlineCombiner extends Reducer<KeyType, IntPair, KeyType, IntPair> {

    @Override
    public void reduce(KeyType key, Iterable<IntPair> values, Context context)
            throws IOException, InterruptedException {
        switch (FieldType.getFieldType(key.getCategory())) {
            case TIME_OF_DAY:
            case DAY_OF_WEEK:
            case MONTH_OF_YEAR:
            case CARRIER_AVG:
                context.write(key, getSum(values));
                break;
            case PLANE:
            case CARRIER_TOT:
            case WEATHER_CITY:
            case AIRPORT:
                context.write(key, getTotal(values));
                break;
        }
    }

    private IntPair getSum(Iterable<IntPair> values) {
        int sum = 0;
        int total = 0;
        for (IntPair val : values) {
            sum += val.getFirst();
            total += val.getSecond();
        }
        return new IntPair(sum, total);
    }

    private IntPair getTotal(Iterable<IntPair> values) {
        int total = 0;
        for (IntPair val : values) {
            total += val.getFirst();
        }
        return new IntPair(total, 0);
    }
}
