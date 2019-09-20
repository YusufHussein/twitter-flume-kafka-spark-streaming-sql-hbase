package CS523.FinalProject.kafkaspakstreaming;

import java.util.*;

import kafka.serializer.StringDecoder;

import org.apache.spark.SparkConf;
import org.apache.spark.TaskContext;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import scala.Tuple2;

public class SparkStreamingFromKafka {

	public static void main(String[] args) throws InterruptedException {
		SparkConf conf = new SparkConf();
		conf.setMaster("local[2]");
		conf.setAppName("Test");
		JavaStreamingContext streamingContext = new JavaStreamingContext(conf,
				Durations.seconds(5));
		Map<String, Object> kafkaParams = new HashMap<>();
		kafkaParams.put("bootstrap.servers", "localhost:9092");
		kafkaParams.put("key.deserializer", StringDeserializer.class);
		kafkaParams.put("value.deserializer", StringDeserializer.class);
		kafkaParams.put("group.id", "0");
		kafkaParams.put("auto.offset.reset", "latest");
		kafkaParams.put("enable.auto.commit", false);

		Set<String> topics = Collections.singleton("test");

		final JavaInputDStream<ConsumerRecord<String, String>> stream = KafkaUtils
				.createDirectStream(streamingContext, LocationStrategies
						.PreferConsistent(), ConsumerStrategies
						.<String, String> Subscribe(topics, kafkaParams));

		// Read value of each message from Kafka and return it
		JavaDStream<String> lines = stream.map(kafkaRecord -> kafkaRecord
				.value());

		// Break every message into words and return list of words
		JavaDStream<String> words = lines.flatMap(line -> Arrays.asList(
				line.split(" ")).iterator());

		// Take every word and return Tuple with (word,1)
		JavaPairDStream<String, Integer> wordMap = words
				.mapToPair(word -> new Tuple2<>(word, 1));

		// Count occurance of each word
		JavaPairDStream<String, Integer> wordCount = wordMap.reduceByKey((
				first, second) -> first + second);

		// Print the word count
		wordCount.print();

		streamingContext.start();
		streamingContext.awaitTermination();
	}
}
