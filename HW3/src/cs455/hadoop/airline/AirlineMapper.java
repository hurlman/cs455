package cs455.hadoop.airline;

import cs455.hadoop.types.FieldType;
import cs455.hadoop.types.KeyType;
import cs455.hadoop.types.RecordData;
import cs455.hadoop.util.Dictionary;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class AirlineMapper extends Mapper<LongWritable, Text, KeyType, IntWritable> {
    private IntWritable one;
    private Dictionary dict;

    @Override
    protected void setup(Context context) throws IOException {
        Configuration conf = context.getConfiguration();
        dict = new Dictionary();
        dict.initialize(conf);
        one = new IntWritable(1);
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        try {
            // Skip header line.
            if (key.get() == 0 && value.toString().contains("CRSDepTime"))
                return;

            RecordData rec = new RecordData(dict, value);

            // Questions 1 and 2
            context.write(rec.getDepTime(), rec.getDepDelay());
            context.write(rec.getDepDay(), rec.getDepDelay());
            context.write(rec.getDepMonth(), rec.getDepDelay());

            // Question 3
            if (rec.getDestAirport() != null) {
                String destArptByYr = String.format("%s-%s",
                        rec.getYear(), rec.getDestAirport().getIata());
                context.write(new KeyType(FieldType.AIRPORT, destArptByYr), one);
            }
            if (rec.getOriginAirport() != null) {
                String orgArptByYr = String.format("%s-%s",
                        rec.getYear(), rec.getOriginAirport().getIata());
                context.write(new KeyType(FieldType.AIRPORT, orgArptByYr), one);
            }

            // Question 4
            if (rec.isDelayed()) {
                context.write(new KeyType(FieldType.CARRIER_TOT,
                        rec.getCarrier()), one);
                context.write(new KeyType(FieldType.CARRIER_AVG,
                        rec.getCarrier()), rec.getDepDelay());
            }

            // Question 5
            if (rec.arrivedLate() && rec.getPlane() != null) {
                if (rec.getPlane().isOld(rec.getYear())) {
                    context.write(new KeyType(FieldType.PLANE, "OLD"), one);
                } else {
                    context.write(new KeyType(FieldType.PLANE, "NEW"), one);
                }
            }
            // Question 6
            if (rec.hasWeatherDelay()) {
                if (rec.getOriginAirport() != null) {
                    String orgCity = rec.getOriginAirport().getCity() + "-" +
                            rec.getOriginAirport().getState();
                    context.write(new KeyType(FieldType.WEATHER_CITY, orgCity), one);
                }
                if (rec.getDestAirport() != null) {
                    String destCity = rec.getDestAirport().getCity() + "-" +
                            rec.getOriginAirport().getState();
                    context.write(new KeyType(FieldType.WEATHER_CITY, destCity), one);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
