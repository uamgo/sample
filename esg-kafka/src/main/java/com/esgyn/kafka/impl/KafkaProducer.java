package com.esgyn.kafka.impl;
import java.util.*;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
 
public class KafkaProducer {
    public static void produce() {
        Properties props = new Properties();
        props.put("metadata.broker.list", "localhost:9092");
        /*props.put("serializer.class", "kafka.serializer.StringEncoder");*/
        props.put("serializer.class", "com.esgyn.kafka.impl.JsonEncoder");
        props.put("partitioner.class", "com.esgyn.kafka.impl.SimplePartitioner");
        props.put("request.required.acks", "0");
 
        ProducerConfig config = new ProducerConfig(props);
 
        Producer<String, JSONObject> producer = new Producer<String, JSONObject>(config);
        final ObjectMapper objectMapper = new ObjectMapper();
        	   JSONObject jmsg = new JSONObject();
        	   String msg=null;
        	   try {
        		   jmsg.append("name","tom");
        		   jmsg.append("words", "are you there");
				/*try {
					msg =objectMapper.writeValueAsString(jmsg);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
               KeyedMessage<String, JSONObject> data = new KeyedMessage<String, JSONObject>("topic1", jmsg);
               producer.send(data);
               producer.close();
    }
}