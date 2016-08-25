package com.esgyn.service.jdbc;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.log4j.Logger;

public interface EJdbc {

	void open() throws SQLException;

	void close() throws SQLException;

	Map<Integer,String> getCurrentOffset();

	public List<String> getOffsetList();

	void insert(ConsumerRecords<String, String> records, Logger logger) throws Exception;

}
