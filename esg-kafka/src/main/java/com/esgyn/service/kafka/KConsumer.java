package com.esgyn.service.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecords;

public interface KConsumer {

	public void commit();

	public ConsumerRecords<String, String> poll(long i);

	void seek(int partition, long offset);

	/*void await() throws InterruptedException;*/

	long getCount();

	public void countDown();
}
