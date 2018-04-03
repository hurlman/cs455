package cs455.hadoop.airline;

import cs455.hadoop.types.IntPair;
import cs455.hadoop.types.KeyType;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Main job class.  Single MapReduce job answers all questions with one pass through data.
 */
public class AirlineJob {

    public static void main(String[] args) {
        try {
            Configuration conf = new Configuration();

            Job job = Job.getInstance(conf, "airline");
            job.setJarByClass(AirlineJob.class);

            job.setMapperClass(AirlineMapper.class);
            job.setCombinerClass(AirlineCombiner.class);
            job.setReducerClass(AirlineReducer.class);

            job.setMapOutputKeyClass(KeyType.class);
            job.setMapOutputValueClass(IntPair.class);

            job.setOutputKeyClass(KeyType.class);
            job.setOutputValueClass(IntWritable.class);

            FileInputFormat.addInputPath(job, new Path(args[0]));
            FileOutputFormat.setOutputPath(job, new Path(args[1]));

            System.exit(job.waitForCompletion(true) ? 0 : 1);
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }
}
