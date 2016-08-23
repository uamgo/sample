package com.esgyn.service.jdbc;

import java.sql.SQLException;

import org.apache.kafka.clients.consumer.ConsumerRecords;

public interface EJdbc {

	void open() throws SQLException;

	void close() throws SQLException;

	long getCurrentOffset();

	void insert(ConsumerRecords<String, String> records, long savedOffset) throws Exception;

}
