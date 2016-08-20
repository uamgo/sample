package com.esgyn.kafka.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esgyn.service.jdbc.EJdbc;
import com.esgyn.service.jdbc.EsgDatasource;
import com.esgyn.service.kafka.KConsumer;

public class Runner {
	private static Logger log = LoggerFactory.getLogger(Runner.class);

	public static void main(String[] args)
			throws FileNotFoundException, IOException, URISyntaxException, SQLException {
		Properties p = new Properties();
		InputStream input = null;
		if (args.length > 0) {
			try {
				input = new FileInputStream(args[0]);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		if (input == null) {
			input = Runner.class.getResource("/config.properties").openStream();
		}
		p.load(input);

		if (input != null) {
			input.close();
		}
		String consumerImpl = p.getProperty("KConsumerImpl", "com.esgyn.kafka.impl.KConsumerImpl");
		String jdbcImpl = p.getProperty("EJdbcImpl", "com.esgyn.kafka.impl.EJdbcImpl");
		long pollTimeout = Integer.valueOf(p.getProperty("poll.timeout", "10000"));
		KConsumer consumer = null;
		try {
			consumer = (KConsumer) Class.forName(consumerImpl).getConstructors()[0].newInstance(p);
		} catch (Exception e) {
			consumer = new KConsumerImpl(p);
			log.error(e.getMessage(), e);
		}
		EJdbc ej = null;
		try {
			ej = (EJdbc) Class.forName(jdbcImpl).getConstructors()[0].newInstance(p);
		} catch (Exception e) {
			ej = new EJdbcImpl(p);
			log.error(e.getMessage(), e);
		}

		EsgDatasource.addConfig(p);
		boolean hasRecords = true;
		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(pollTimeout);
			hasRecords = records != null && !records.isEmpty();
			try {
				if (hasRecords) {
					ej.open();
					ej.insert(records);
					consumer.commit();
				}
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				if (hasRecords)
					ej.close();
			}
		}

	}

}
