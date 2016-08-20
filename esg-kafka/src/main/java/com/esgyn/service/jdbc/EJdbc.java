package com.esgyn.service.jdbc;

import org.apache.kafka.clients.consumer.ConsumerRecords;

public interface EJdbc {

	void prepare();

	void open();

	void insert(ConsumerRecords<String, String> records);

	void close();

}
