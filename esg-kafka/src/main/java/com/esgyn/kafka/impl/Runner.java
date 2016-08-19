package com.esgyn.kafka.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class Runner {

	public static void main(String[] args) throws FileNotFoundException, IOException, URISyntaxException {
		Properties p = new Properties();
		String path = new File(Runner.class.getResource("/config.properties").toURI()).getAbsolutePath();
		if (args.length > 0) {
			path = args[0];
		}
		p.load(new FileInputStream(path));
		KConsumerImpl consumer = new KConsumerImpl(p);
		consumer.start();
	}

}
