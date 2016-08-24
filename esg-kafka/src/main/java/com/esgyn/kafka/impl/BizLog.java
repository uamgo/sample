package com.esgyn.kafka.impl;

import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SyslogAppender;

public class BizLog {
	public static final int BIZLEVEL = 80000;

	/**
	 * 继承Level
	 * 
	 * @author Sevencm
	 * 
	 */
	private static class CustomerLogLevel extends Level {
		public CustomerLogLevel(int level, String levelStr, int syslogEquivalent) {
			super(level, levelStr, syslogEquivalent);
		}
	}

	/**
	 * 自定义级别名称，以及级别范围
	 */
	private static final Level CustomerLevel = new CustomerLogLevel(BIZLEVEL, "BIZLOG", SyslogAppender.LOG_LOCAL0);

	/**
	 * 使用日志打印logger中的log方法
	 * 
	 * @param logger
	 * @param objLogInfo
	 */
	public static void customerLog(Logger logger, ConsumerRecords<String, String> records,List<String> offset) {
		for (ConsumerRecord<String, String> r : records) {
			if(!offset.contains(String.valueOf(r.offset()))){
				logger.log(CustomerLevel, r.value());
			}
		}
	}

	public static void customerLog(Logger logger, String value) {
		// TODO Auto-generated method stub
		logger.log(CustomerLevel, value);
	}

}