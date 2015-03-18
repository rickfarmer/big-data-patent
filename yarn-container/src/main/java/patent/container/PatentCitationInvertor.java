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

@Configuration
@EnableAutoConfiguration
public class PatentCitationInvertor {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(PatentCitationInvertor.class, args);

		org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err
					.println("Usage: patentcitationinvertor <input directory> <output directory>");
			System.exit(2);
		}

		Path in = new Path(otherArgs[0]);
		Path out = new Path(otherArgs[1]);

		System.out.println("###IN=" + in + ", OUT=" + out);

		Job job = new Job(conf, "Patent Citation Invertor");
		job.setJarByClass(PatentCitationInvertor.class);
		job.setMapperClass(MapperClass.class);
		//job.setCombinerClass(PatentReducerClass.class);
		job.setReducerClass(ReducerClass.class);
		job.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, in);
		FileOutputFormat.setOutputPath(job, out);
		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

	public static class MapperClass extends Mapper<Text, Text, Text, Text> {

		public void map(Text key, Text value, Context context)
				throws IOException, InterruptedException {
			context.write(value, key);
		}
	}

	public static class ReducerClass extends
			Reducer<Text, Text, Text, IntWritable> {

		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {

			int count = 0;
			for (IntWritable val : values) {
				count += val.get();
			}
			context.write(key, new IntWritable(count));

		}
	}
}
