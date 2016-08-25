package com.esgyn.kafka.impl;

import com.esgyn.service.kafka.KConsumer;

public class ThreadAwait extends Thread {
	private KConsumer consumer;
	public ThreadAwait(KConsumer consumer){
		consumer=consumer;
	}
	@Override
	public void run(){
		try {
			sleep(60000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(consumer.getCount()>0){
			consumer.countDown();
		}
	}
}

