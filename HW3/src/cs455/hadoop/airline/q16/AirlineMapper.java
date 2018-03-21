package cs455.hadoop.airline.q16;

import cs455.hadoop.airline.KeyType;
import cs455.hadoop.airline.RecordData;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class AirlineMapper extends Mapper<LongWritable, Text, KeyType, IntWritable> {
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        RecordData rec = new RecordData(value);

        try {
            context.write(rec.getDepTime(), rec.getDepDelay());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
