package com.esgyn.service.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecords;

public interface KConsumer {
	public void start();

	public void commit();

	public ConsumerRecords<String, String> poll(long i);
}
