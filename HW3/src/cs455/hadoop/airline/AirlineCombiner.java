package cs455.hadoop.airline;

import cs455.hadoop.types.FieldType;
import cs455.hadoop.types.IntPair;
import cs455.hadoop.types.KeyType;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Combiner class performs two separate functions, depending on key type.  Either totals or
 * creates running sum and count for averaging in the reducer.  The combiner necessitated the
 * creation of the custom IntPair value class for doing averages.
 *
 * The combiner DRAMATICALLY improved performance during the reduce phase, and did not slow
 * down the map phase at all.
 */
public class AirlineCombiner extends Reducer<KeyType, IntPair, KeyType, IntPair> {

    @Override
    public void reduce(KeyType key, Iterable<IntPair> values, Context context)
            throws IOException, InterruptedException {
        switch (FieldType.getFieldType(key.getCategory())) {
            case TIME_OF_DAY:
            case DAY_OF_WEEK:
            case MONTH_OF_YEAR:
            case CARRIER_AVG:
            case PLANE_AGE:
                context.write(key, getRunningSum(values));
                break;
            case CARRIER_TOT:
            case CARRIER_MIN:
            case WEATHER_CITY:
            case AIRPORT:
            case PLANE_MODEL:
            case PLANE_MODEL_FLIGHTS:
            case PLANE_NUMBER:
                context.write(key, getTotal(values));
                break;
        }
    }

    private IntPair getRunningSum(Iterable<IntPair> values) {
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
