package com.esgyn.kafka.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esgyn.kafka.tools.SaveTool;
import com.esgyn.service.jdbc.EJdbc;
import com.esgyn.service.jdbc.EsgDatasource;
import com.esgyn.service.kafka.KConsumer;

public class Runner {
	private static Logger log = LoggerFactory.getLogger(Runner.class);
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Runner.class);

	public static void main(String[] args) throws FileNotFoundException, IOException, URISyntaxException, SQLException,
			ConfigurationException, InterruptedException {
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
			try {
				input.close();
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
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
		/* new ThreadAwait(consumer).start(); */
		/* consumer.await(); */
		EJdbc ej = null;
		try {
			ej = (EJdbc) Class.forName(jdbcImpl).getConstructors()[0].newInstance(p);
		} catch (Exception e) {
			ej = new EJdbcImpl(p);
			log.error(e.getMessage(), e);
		}
		SaveTool st = new SaveTool(p);

		EsgDatasource.addConfig(p);
		boolean hasRecords = true;
		boolean hasErr = false;

		while (true) {
			if (st.isStop()) {
				log.warn("Current job will be stopped in a second!");
				System.exit(0);
			}

			hasErr = false;
			Map<Integer, String> savedOffset = st.getSavedOffset();
			for (Entry<Integer, String> offsetEntry : savedOffset.entrySet()) {
				if (Long.valueOf(offsetEntry.getValue()) > -1) {
					consumer.seek(offsetEntry.getKey().intValue(), Long.valueOf(offsetEntry.getValue()) + 1);
				}
			}
			st.reInitialOffset();
			ConsumerRecords<String, String> records = consumer.poll(pollTimeout);
			hasRecords = records != null && !records.isEmpty();
			try {
				if (hasRecords) {
					ej.open();
					ej.insert(records, logger);
				}
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
				hasErr = true;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				hasErr = true;
			} finally {
				if (hasErr) {
					BizLog.customerLog(logger, records, ej.getOffsetList());
				}
				int count = 0;
				boolean flag = false;
				if (hasRecords) {
					do {
						flag = false;
						try {
							consumer.commit();
							// if (new Random().nextInt(10) > 2)
							// throw new Exception("---------Expected
							// Exception---------");
						} catch (Exception e2) {
							flag = true;
							++count;
							if (count >= 3) {
								log.error("[offset: " + ej.getCurrentOffset() + "]", e2);
								// if insertion is success, but commit failed,
								// then
								// save offset
								if (!hasErr)
									st.saveOffset(ej.getCurrentOffset());
							}
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
							}
						}
					} while (flag && count < 3);
					try {
						ej.close();
					} catch (Exception e) {
						log.error("Something wrong with the connection, will sleep 5 secords", e);
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {
						}
					}
				}
			}
		}

	}

}
