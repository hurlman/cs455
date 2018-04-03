package cs455.hadoop.airline;

import cs455.hadoop.types.FieldType;
import cs455.hadoop.types.IntPair;
import cs455.hadoop.types.KeyType;
import cs455.hadoop.types.RecordData;
import cs455.hadoop.util.Dictionary;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Mapper class.  For each line read, passes line into a Record class for parsing.
 * Creates keys and values and writes to context based on the data.  Both the Key and Value
 * are custom classes.  The key contains a data category, such as TIME_OF_DAY, and a value,
 * such as '0900'.  The value is an Integer Pair.  A pair is needed for performing averages
 * with a combiner.  A combiner was needed to dramatically improve performance.
 */
public class AirlineMapper extends Mapper<LongWritable, Text, KeyType, IntPair> {
    private Dictionary dict;

    /**
     * Initializing a dictionary of supplemental data (plane, airport, and carrier reference
     * data).  This is passed into and used by the Record class.
     */
    @Override
    protected void setup(Context context) throws IOException {
        Configuration conf = context.getConfiguration();
        dict = new Dictionary();
        dict.initialize(conf);
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        try {
            // Skip header line.
            if (key.get() == 0 && value.toString().contains("CRSDepTime"))
                return;

            RecordData rec = new RecordData(dict, value);

            // Questions 1 and 2
            context.write(rec.getDepTime(), new IntPair(rec.getDepDelay(), 1));
            context.write(rec.getDepDay(), new IntPair(rec.getDepDelay(), 1));
            context.write(rec.getDepMonth(), new IntPair(rec.getDepDelay(), 1));

            // Question 3
            if (rec.getDestAirport() != null) {
                String destArptByYr = String.format("%s-%s",
                        rec.getYear(), rec.getDestAirport().getAirport());
                context.write(new KeyType(FieldType.AIRPORT, destArptByYr), new IntPair(1, 0));
            }
            if (rec.getOriginAirport() != null) {
                String orgArptByYr = String.format("%s-%s",
                        rec.getYear(), rec.getOriginAirport().getAirport());
                context.write(new KeyType(FieldType.AIRPORT, orgArptByYr), new IntPair(1, 0));
            }

            // Question 4
            if (rec.isDelayed()) {
                context.write(new KeyType(FieldType.CARRIER_TOT,
                        rec.getCarrier()), new IntPair(1, 0));
                context.write(new KeyType(FieldType.CARRIER_MIN,
                        rec.getCarrier()), new IntPair(rec.getDepDelay(), 0));
                context.write(new KeyType(FieldType.CARRIER_AVG,
                        rec.getCarrier()), new IntPair(rec.getDepDelay(), 1));
            }

            // Question 5
            if (rec.arrivedLate() && rec.getPlane() != null) {
                if (rec.getPlane().isOld(rec.getYear())) {
                    context.write(new KeyType(FieldType.PLANE_AGE, "OLD"),
                            new IntPair(rec.getDepDelay(), 1));
                } else {
                    context.write(new KeyType(FieldType.PLANE_AGE, "NEW"),
                            new IntPair(rec.getDepDelay(), 1));
                }
            }

            // Question 6
            if (rec.hasWeatherDelay()) {
                if (rec.getOriginAirport() != null) {
                    String orgCity = rec.getOriginAirport().getCity() + "-" +
                            rec.getOriginAirport().getState();
                    context.write(new KeyType(FieldType.WEATHER_CITY, orgCity), new IntPair(1, 0));
                }
                if (rec.getDestAirport() != null) {
                    String destCity = rec.getDestAirport().getCity() + "-" +
                            rec.getOriginAirport().getState();
                    context.write(new KeyType(FieldType.WEATHER_CITY, destCity), new IntPair(1, 0));
                }
            }

            // Question 7
            if (rec.getPlane() != null) {
                context.write(new KeyType(FieldType.PLANE_NUMBER, rec.getPlane().getTailnum()),
                        new IntPair(rec.getDistance(), 0));
                context.write(new KeyType(FieldType.PLANE_MODEL, rec.getPlane().getModel()),
                        new IntPair(rec.getDistance(), 0));
                context.write(new KeyType(FieldType.PLANE_MODEL_FLIGHTS, rec.getPlane().getModel()),
                        new IntPair(1, 0));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
