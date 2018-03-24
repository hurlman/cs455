package cs455.hadoop.airline;

import cs455.hadoop.types.KeyType;
import cs455.hadoop.util.Dictionary;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class AirlineJob {

    public static void main(String[] args) {
        try {
            Configuration conf = new Configuration();
            Dictionary.getInstance().initialize(conf);

            // First job makes pass over source data.

            Job job = Job.getInstance(conf, "airline");
            job.setJarByClass(AirlineJob.class);

            job.setMapperClass(AirlineMapper.class);
            job.setReducerClass(AirlineReducer.class);

            job.setOutputKeyClass(KeyType.class);
            job.setOutputValueClass(IntWritable.class);

            // Increases speed but output is unsorted.
            job.setNumReduceTasks(12);

            FileInputFormat.addInputPath(job, new Path(args[0]));
            FileOutputFormat.setOutputPath(job, new Path(args[1]));

            job.waitForCompletion(true);

            // Second runs on output of first job.  Flips kvp, sorts.



        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }
}
