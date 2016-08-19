package esgyn.kafka;

import java.util.List;

import org.codehaus.jettison.json.JSONObject;

public interface KafkaConsumerReader {
	public void readJsonFromKafka(String a_topic, int a_partition, List<String> a_seedBrokers, int a_port) throws Exception;
	public void insertTrafodion(JSONObject obj);
}
