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
import org.apache.hadoop.util.GenericOptionsParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class PatentHistogram {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(PatentHistogram.class, args);

		org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err
					.println("Usage: patenthistogram <input directory> <output directory>");
			System.exit(2);
		}

		Path in = new Path(otherArgs[0]);
		Path out = new Path(otherArgs[1]);

		System.out.println("###IN=" + in + ", OUT=" + out);

		Job job = new Job(conf, "Patent Histogram");
		job.setJarByClass(PatentHistogram.class);
		job.setMapperClass(PatentHistogramMapperClass.class);
		job.setReducerClass(PatentHistogramReducerClass.class);
		job.setInputFormatClass(KeyValueTextInputFormat.class);
		//job.setOutputFormatClass(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		FileInputFormat.addInputPath(job, in);
		FileOutputFormat.setOutputPath(job, out);
		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

	public static class PatentHistogramMapperClass extends
			Mapper<Text, Text, Text, IntWritable> {

		public void map(Text key, Text value, Context context)
				throws IOException, InterruptedException {

			System.out.println("###PatentHistogramMapperClass");
			
			// calculate a “histogram” of data telling us how many patents were
			// generated by each state entity (country or US state)

			// System.out.println("###KEY=" + key + ", VALUE=" + value);
			// e.g. ###KEY=EE, VALUE=5486013,5459074,5375053,5858298

			String[] patent = (value.toString().split(","));
			//System.out.println("###KEY=" + key + ", VALUE=" + patent.length);
			context.write(key, new IntWritable(patent.length));

		}
	}

	public static class PatentHistogramReducerClass extends
			Reducer<Text, IntWritable, Text, IntWritable> {

		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {

			System.out.println("###PatentHistogramReducerClass");
			
			// System.out.println(values.toString());
			int count = 0;
			for (IntWritable val : values) {
				count += val.get();
			}

			context.write(key, new IntWritable(count));

		}
	}

}
