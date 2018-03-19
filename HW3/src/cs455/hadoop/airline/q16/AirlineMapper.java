package cs455.hadoop.airline.q16;

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

        int delayMin;
        try{
            delayMin = Integer.parseInt(recordData[14]);
        }catch(NumberFormatException ne){
            delayMin = 0;
        }

        context.write(new Text(recordData[5]), new IntWritable(delayMin));
    }
}
