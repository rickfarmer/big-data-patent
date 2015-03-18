package patent.container;

//import org.apache.hadoop.conf.Configuration;
import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import patent.container.Patent.PatentMapperClass;
import patent.container.Patent.PatentReducerClass;
import patent.container.PatentHistogram.PatentHistogramMapperClass;
import patent.container.PatentHistogram.PatentHistogramReducerClass;
import utils.TextUtil;

@Configuration
@EnableAutoConfiguration
public class PatentChainedHistogram {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(PatentChainedHistogram.class, args);

		org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err
					.println("Usage: patentchainedhistogram <input directory> <output directory>");
			System.exit(2);
		}

		Path in = new Path(otherArgs[0]);
		Path temp = new Path("temp");
		Path out = new Path(otherArgs[1]);

		System.out.println("JOB1 ###IN=" + in + ", OUT=" + temp);
	

		Job job1 = new Job(conf, "Patent Chained Histogram");
		job1.setJarByClass(Patent.class);
		job1.setMapperClass(PatentMapperClass.class);
		job1.setReducerClass(PatentReducerClass.class);
		job1.setInputFormatClass(KeyValueTextInputFormat.class);
		job1.setOutputFormatClass(TextOutputFormat.class);
		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job1, in);
		FileOutputFormat.setOutputPath(job1, temp);
		//System.exit(job1.waitForCompletion(true) ? 0 : 1);
		job1.waitForCompletion(true);
		
		System.out.println("JOB2 ###IN=" + temp + ", OUT=" + out);
		
		Job job2 = new Job(conf, "Patent Histogram");
		job2.setJarByClass(PatentHistogram.class);
		job2.setMapperClass(PatentHistogramMapperClass.class);
		job2.setReducerClass(PatentHistogramReducerClass.class);
		job2.setInputFormatClass(KeyValueTextInputFormat.class);
		//job.setOutputFormatClass(TextOutputFormat.class);
		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(IntWritable.class);

		FileInputFormat.addInputPath(job2, temp);
		FileOutputFormat.setOutputPath(job2, out);
		System.exit(job2.waitForCompletion(true) ? 0 : 1);

	}

}
