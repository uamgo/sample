package com.esgyn.service.jdbc;

import java.sql.SQLException;

import org.apache.kafka.clients.consumer.ConsumerRecords;

public interface EJdbc {

	void open() throws SQLException;

	void insert(ConsumerRecords<String, String> records) throws Exception;

	void close() throws SQLException;

}
