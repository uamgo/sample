package com.esgyn.kafka.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esgyn.service.kafka.KConsumer;

public class KConsumerImpl implements KConsumer {
	private static final Logger log = LoggerFactory.getLogger(KConsumerImpl.class);
	private final KafkaConsumer<String, String> consumer;
	private String topic;
	private Properties config;
	CountDownLatch cd = new CountDownLatch(1);

	public KConsumerImpl(Properties config) {
		this.config=config;
		consumer = new KafkaConsumer<>(config);
		this.topic = config.getProperty("topic");
		List<PartitionInfo> ps = consumer.partitionsFor(topic);
		List<TopicPartition> partitions = new ArrayList<TopicPartition>();
		for(PartitionInfo info: ps){
			partitions.add(new TopicPartition(this.topic,info.partition()));
		}
		consumer.assign(partitions);
	}

	@Override
	public void commit() {
		consumer.commitSync();
	}
	@Override
	public void seek(int partition,long offset) {
		consumer.seek(new TopicPartition(this.topic,partition), offset);
	}
	@Override
	public ConsumerRecords<String, String> poll(long timeWindow) {
		return consumer.poll(timeWindow);
	}
	
	/*@Override
	public void await() throws InterruptedException{
		cd.await();
	}*/
	@Override
	public void countDown(){
		cd.countDown();
	}
	@Override
	public long getCount(){
		return cd.getCount();
	}

}
