package com.esgyn.kafka.impl;  
  
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.log4j.Level;  
import org.apache.log4j.Logger;  
import org.apache.log4j.net.SyslogAppender;  
  
public class BizLog {    
      
    /** 
     * 继承Level 
     * @author Sevencm 
     * 
     */  
    private static class CustomerLogLevel extends Level{  
        public CustomerLogLevel(int level, String levelStr, int syslogEquivalent) {  
            super(level, levelStr, syslogEquivalent);  
        }         
    }  
      
    /** 
     * 自定义级别名称，以及级别范围 
     */  
    private static final Level CustomerLevel = new CustomerLogLevel(1000,"BACKUP",SyslogAppender.LOG_LOCAL0);  
      
    /** 
     * 使用日志打印logger中的log方法 
     *  
     * @param logger 
     * @param objLogInfo 
     */  
    public static void customerLog(Logger logger,ConsumerRecords<String, String> records){ 
    	for (ConsumerRecord<String, String> r : records) {
    		logger.log(CustomerLevel, r.value());  
    	}
    }  
      
      
      
}  