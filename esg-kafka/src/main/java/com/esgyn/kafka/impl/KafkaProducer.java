package com.esgyn.kafka.impl;

import java.util.Properties;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class KafkaProducer {
	public static void produce() {
		Properties props = new Properties();
		props.put("metadata.broker.list", "10.10.10.136:9092");
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		// props.put("serializer.class", "com.esgyn.kafka.impl.JsonEncoder");
//		props.put("partitioner.class", "com.esgyn.kafka.impl.SimplePartitioner");
		props.put("request.required.acks", "0");

		ProducerConfig config = new ProducerConfig(props);
		String smsg = "{																											  "+
				"  \"MetricsName\": \"cpu/limit\",																							  "+
				"  \"MetricsValue\": {																									  "+
				"    \"value\": 0																									  "+
				"  },																											  "+
				"  \"MetricsTimestamp\": \"2016-08-17T02:41:00Z\",																					  "+
				"  \"MetricsTags\": {																									  "+
				"    \"container_base_image\": \"172.16.16.210:5000/blit/elasticsearch:alpine-2.3.2-rootmodel-with-sql\",														  "+
				"    \"container_name\": \"elasticsearch\",																						  "+
				"    \"host_id\": \"172.16.16.215\",																							  "+
				"    \"hostname\": \"172.16.16.215\",																							  "+
				"    \"labels\": \"esnode-c5245fajpi:esnode-c5245fajpi,ests-h3ppt1atk7:ests-h3ppt1atk7,ests1-jz3cyqdquv:ests1-jz3cyqdquv,k8s-app:20160816152311f524tl66drd5cf,kubernetes.io/cluster-service:true,version:20160816152714\","+
				"    \"namespace_id\": \"476a9cfd-3ce6-11e6-8379-0cc47aaaa95d\",																			  "+
				"    \"namespace_name\": \"default\",																							  "+
				"    \"nodename\": \"172.16.16.215\",																							  "+
				"    \"pod_id\": \"54eda7af-6383-11e6-929d-0cc47aaaa95d\",																				  "+
				"    \"pod_name\": \"esnode1-xmw3wfblw4-ysp8e\",																					  "+
				"    \"pod_namespace\": \"default\",																							  "+
				"    \"type\": \"pod_container\"																							  "+
				"  }																											  "+
				"}																											  ";
		
		
		
		Producer<String, String> producer = new Producer<String, String>(config);
		KeyedMessage<String, String> data = new KeyedMessage<String, String>("topic1", smsg);
		producer.send(data);
		producer.close();
	}

	public static void main(String[] args) {
		produce();
	}
}