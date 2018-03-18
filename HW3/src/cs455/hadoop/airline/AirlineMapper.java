package cs455.hadoop.airline;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.StringTokenizer;

public class AirlineMapper extends Mapper<LongWritable, Text, Text,IntWritable> {
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String record = value.toString();
        String recordData[] = record.split("\\s*,\\s*");

        context.write(new Text(recordData[5]), new IntWritable(Integer.parseInt(recordData[14])));
    }
}
