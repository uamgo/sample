package com.esgyn.service.jdbc;

import java.sql.SQLException;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.log4j.Logger;

public interface EJdbc {

	void open() throws SQLException;

	void close() throws SQLException;

	long getCurrentOffset();

	public List<String> getOffsetList();

	void insert(ConsumerRecords<String, String> records, long savedOffset, Logger logger) throws Exception;

}
