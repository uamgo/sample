package com.esgyn.service.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esgyn.kafka.impl.Runner;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class EsgDatasource {
	private static Logger log = LoggerFactory.getLogger(EsgDatasource.class);
	private static Properties _config = new Properties();
	private static HikariDataSource ds = null;

	public static void addConfig(Properties config) {
		for (Entry<Object, Object> entry : config.entrySet()) {
			if (entry.getKey().toString().startsWith("db.")) {
				_config.put(entry.getKey().toString().substring(3), entry.getValue());
			}
		}
		if (ds != null) {
			try {
				ds.close();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}

		HikariConfig conf = new HikariConfig(_config);
		ds = new HikariDataSource(conf);
	}

	public static Connection getConn() throws SQLException {
		return ds.getConnection();
	}

	public static void close() {
		if (ds != null) {
			ds.close();
		}
	}

	public static void main(String[] args) throws SQLException, IOException {
		Properties config = new Properties();
		config.load(Runner.class.getResource("/config.properties").openStream());
		Properties p = new Properties();
		for (Entry<Object, Object> entry : config.entrySet()) {
			if (entry.getKey().toString().startsWith("db.")) {
				p.put(entry.getKey().toString().substring(3), entry.getValue());
			}
		}
		addConfig(p);

		Connection conn = getConn();
		ResultSet rs = conn.createStatement().executeQuery("values(1)");
		if (rs.next()) {
			log.warn("result:   " + rs.getObject(1));
		}
		conn.close();
		close();
	}

}
