package patent.container;

//import org.apache.hadoop.conf.Configuration;
import java.io.IOException;

import org.apache.hadoop.fs.Path;
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

import utils.TextUtil;

@Configuration
@EnableAutoConfiguration
public class Patent {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Patent.class, args);

		org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err
					.println("Usage: patent <input directory> <output directory>");
			System.exit(2);
		}

		Path in = new Path(otherArgs[0]);
		Path out = new Path(otherArgs[1]);

		// System.out.println("###IN=" + in + ", OUT=" + out);

		Job job = new Job(conf, "Patent By Country");
		job.setJarByClass(Patent.class);
		job.setMapperClass(PatentMapperClass.class);
		job.setReducerClass(PatentReducerClass.class);
		job.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, in);
		FileOutputFormat.setOutputPath(job, out);
		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

	public static class PatentMapperClass extends Mapper<Text, Text, Text, Text> {

		public void map(Text key, Text value, Context context)
				throws IOException, InterruptedException {

			// Tell us which patents were granted for every country of the first
			// inventor. Treat US states as separate entities (countries).

			//System.out.println("###PatentMapperClass");
			
			// System.out.println("###KEY=" + key + ", VALUE=" + value);
			String[] patent = (key.toString().split(","));

			if (patent.length > 5) {

				key = new Text(TextUtil.removeQuotes(patent[4] + patent[5])
						.trim());
				value = new Text(patent[0]);

				// System.out.println("###KEY=" + key + ", VALUE=" + value);
				context.write(key, value);
			}
		}
	}

	public static class PatentReducerClass extends Reducer<Text, Text, Text, Text> {

		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {

			//System.out.println("###PatentReducerClass");
			
			// System.out.println(values.toString());
			boolean isFirstElement = true;
			String combined = new String();
			for (Text val : values) {
				combined += (isFirstElement) ? val.toString() : ","
						+ val.toString();
				isFirstElement = false;
			}
			context.write(key, new Text(combined));

		}
	}
}
