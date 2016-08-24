package com.esgyn.kafka.tools;

import java.io.File;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveTool {
	Logger log = LoggerFactory.getLogger(SaveTool.class);
	private PropertiesConfiguration offsetConfig;
	private PropertiesConfiguration switcher;

	public SaveTool(Properties p) throws ConfigurationException {
		offsetConfig = new PropertiesConfiguration(new File(p.getProperty("offset")));
		offsetConfig.setAutoSave(false);
		log.warn("base path for offsetConfig:" + offsetConfig.getBasePath());
		/*
		 * switcher = new PropertiesConfiguration(new
		 * File("config/status.properties"));
		 */
		/*
		 * log.warn("base path for switcher:"+switcher.getBasePath());
		 * switcher.save();
		 */
	}

	public long getSavedOffset() throws ConfigurationException {
		offsetConfig.refresh();
		return offsetConfig.getLong("offset", 0L);
	}

	public boolean isStop() throws ConfigurationException {
		offsetConfig.refresh();
		return offsetConfig.getString("status", "start").trim().toLowerCase().equals("stop");
	}

	public void saveOffset(long currentOffset) throws ConfigurationException {
		offsetConfig.setProperty("offset", currentOffset);
		offsetConfig.save();
	}

}
