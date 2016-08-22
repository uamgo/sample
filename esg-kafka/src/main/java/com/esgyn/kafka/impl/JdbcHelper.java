package com.esgyn.kafka.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


public class JdbcHelper {
	private static final Logger _LOG = LoggerFactory.getLogger(JdbcHelper.class);
	private static JdbcHelper jdbcHelper;
	private static HikariDataSource ds=null;
	private static Connection conn=null;

	private JdbcHelper(Properties p) {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(p.getProperty("jdbc.url"));
		config.setUsername(p.getProperty("name"));
		config.setPassword(p.getProperty("pwd"));
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		ds = new HikariDataSource(config);
	}

	public synchronized static final JdbcHelper getInstance(Properties p) {
		if (jdbcHelper == null) {
			try {
				jdbcHelper = new JdbcHelper(p);
				conn=ds.getConnection();
			} catch (Exception e) {
				_LOG.error(e.getMessage(), e);
			}
		}
		return jdbcHelper;
	}
	public void insert(ConsumerRecords<String, String> records) {
		for (ConsumerRecord<String, String> consumerRecord : records) {
			/*consumerRecord.*/
		}
	}
	public void close() {
		try {
			if (conn!=null) {
				conn.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
