package com.esgyn.kafka.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class EsgKafkaProducer {
	public static void produce() throws URISyntaxException, IOException {
		Properties p = new Properties();
		/*p.load(new FileInputStream("config.properties"));*/
		p.load(EsgKafkaProducer.class.getResource("/config.properties").openStream());
		Properties props = new Properties();
		props.put("bootstrap.servers", p.getProperty("bootstrap.servers", "192.168.1.46:9092"));
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		// props.put("serializer.class", "com.esgyn.kafka.impl.JsonEncoder");
		// props.put("partitioner.class",
		// "com.esgyn.kafka.impl.SimplePartitioner");
		props.put("acks", "1");
			
		String smsg = "{																											  "
				+ "  \"MetricsName\": \"cpu/limit\",																							  "
				+ "  \"MetricsValue\": {																									  "
				+ "    \"value\": 0																									  "
				+ "  },																											  "
				+ "  \"MetricsTimestamp\": \"2016-08-17T02:41:00Z\",																					  "
				+ "  \"MetricsTags\": {																									  "
				+ "    \"container_base_image\": \"172.16.16.210:5000/blit/elasticsearch:alpine-2.3.2-rootmodel-with-sql\",														  "
				+ "    \"container_name\": \"elasticsearch\",																						  "
				+ "    \"host_id\": \"172.16.16.215\",																							  "
				+ "    \"hostname\": \"172.16.16.215\",																							  "
				+ "    \"labels\": \"esnode-c5245fajpi:esnode-c5245fajpi,ests-h3ppt1atk7:ests-h3ppt1atk7,ests1-jz3cyqdquv:ests1-jz3cyqdquv,k8s-app:20160816152311f524tl66drd5cf,kubernetes.io/cluster-service:true,version:20160816152714\","
				+ "    \"namespace_id\": \"476a9cfd-3ce6-11e6-8379-0cc47aaaa95d\",																			  "
				+ "    \"namespace_name\": \"default\",																							  "
				+ "    \"nodename\": \"172.16.16.215\",																							  "
				+ "    \"pod_id\": \"54eda7af-6383-11e6-929d-0cc47aaaa95d\",																				  "
				+ "    \"pod_name\": \"esnode1-xmw3wfblw4-ysp8e\",																					  "
				+ "    \"pod_namespace\": \"default\",																							  "
				+ "    \"type\": \"pod_container\"																							  "
				+ "  }																											  "
				+ "}		"
				+ "																									  ";
		/*smsg="ccccdddddmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmdddddddddddddddcccc";*/
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
		String topic = "topic3";
		ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, smsg);
		producer.send(record);
		producer.close();
		System.out.println("success");
	}

	public static void main(String[] args) throws URISyntaxException, IOException {
		produce();
	}
}