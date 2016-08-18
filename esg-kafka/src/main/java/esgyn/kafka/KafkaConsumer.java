package esgyn.kafka;

import java.util.List;

import com.esgyn.model.Metric;

public interface KafkaConsumer {
	public void readJsonFromKafka(String a_topic, int a_partition, List<String> a_seedBrokers, int a_port) throws Exception;
	public void insertTrafodion(Metric metric);
}
