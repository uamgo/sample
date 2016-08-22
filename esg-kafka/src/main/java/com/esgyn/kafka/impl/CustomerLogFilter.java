package com.esgyn.kafka.impl;  
  
import org.apache.log4j.spi.Filter;  
import org.apache.log4j.spi.LoggingEvent;  
  
public class CustomerLogFilter extends Filter {

	@Override
	public int decide(LoggingEvent event) {
		System.out.println("-------------"+event.getLevel());
		if(event.getLevel().toInt() == 1000){
			return 0;
		}
		return -1;
	}  
  
  
}  