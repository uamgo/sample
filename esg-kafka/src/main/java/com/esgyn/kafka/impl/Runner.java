package com.esgyn.kafka.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecords;

import com.esgyn.service.jdbc.EJdbc;
import com.esgyn.service.kafka.KConsumer;

public class Runner {

	public static void main(String[] args) throws FileNotFoundException, IOException, URISyntaxException {
		Properties p = new Properties();
		String path = new File(Runner.class.getResource("/config.properties").toURI()).getAbsolutePath();
		if (args.length > 0) {
			path = args[0];
		}
		p.load(new FileInputStream(path));
		KConsumer consumer = new KConsumerImpl(p);
//		try {
//			consumer = (KConsumer) Class.forName("com.esgyn.kafka.impl.KConsumerImpl").getConstructors()[0]
//					.newInstance(p);
//		} catch (Exception e) {
//
//		}
		EJdbc ej = null;
		ej.prepare(p);
		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(10000);
			ej.open();
			ej.insert(records);
			ej.close();
			consumer.commit();
		}

	}

}
