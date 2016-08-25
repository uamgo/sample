package com.esgyn.kafka.tools;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
		
		 switcher = new PropertiesConfiguration(new
		 File("config/status.properties"));
		 
		
		 log.warn("base path for switcher:"+switcher.getBasePath());
		 switcher.save();
		 
	}

	public Map<Integer,String> getSavedOffset() throws ConfigurationException {
		offsetConfig.refresh();
		Iterator<String> it=offsetConfig.getKeys();
		Map<Integer,String> offsetMap=new HashMap<Integer,String>();
		while (it.hasNext()) {
			String key=it.next();
			offsetMap.put(Integer.valueOf(key), offsetConfig.getString(key,"-1"));
		}
		return offsetMap;
	}

	public boolean isStop() throws ConfigurationException {
		switcher.refresh();
		return switcher.getString("status", "start").trim().toLowerCase().equals("stop");
	}

	public void saveOffset(Map<Integer,String> currentOffsetMap) throws ConfigurationException {
		for (Map.Entry<Integer,String> entry : currentOffsetMap.entrySet()) {
			offsetConfig.setProperty(String.valueOf(entry.getKey()), entry.getValue());
		}
		offsetConfig.save();
	}

	public void reInitialOffset() {
		// TODO Auto-generated method stub
		Iterator<String> keys=offsetConfig.getKeys();
		while (keys.hasNext()) {
			offsetConfig.setProperty(keys.next(), -1);
		}
		try {
			offsetConfig.save();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
