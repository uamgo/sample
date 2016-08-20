package com.esgyn.kafka.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esgyn.service.jdbc.EJdbc;
import com.esgyn.service.jdbc.EsgDatasource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EJdbcImpl implements EJdbc {
	private static Logger log = LoggerFactory.getLogger(EJdbcImpl.class);
	private Connection con;
	private String sql;
	private ObjectMapper mapper;

	public EJdbcImpl(Properties config) {
		EsgDatasource.addConfig(config);
		this.sql = config.getProperty("insertString");
		mapper = new ObjectMapper();
	}

	@Override
	public void open() throws SQLException {
		con = EsgDatasource.getConn();
	}

	@Override
	public void insert(ConsumerRecords<String, String> records) throws Exception {
		// PreparedStatement ps = this.con.prepareStatement(sql);
		for (ConsumerRecord<String, String> r : records) {
			JsonNode root = null;
			try {
				root = mapper.readTree(r.value());
				log.warn("[" + r.offset() + "]" + r.value());
			} catch (Exception e) {
				log.error(r.value() + " [Error]" + e.getMessage());
				continue;
			}

		}

	}

	@Override
	public void close() throws SQLException {
		this.con.close();
	}

}
