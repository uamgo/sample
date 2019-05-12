package com.esgyn.perftest;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class EsgStats {
  private final int total;
  private final long start;
  private AtomicInteger counter = new AtomicInteger(0);

  public EsgStats(Properties conf) {
    this.total = (int) conf.get("total");
    this.start = System.currentTimeMillis();
  }

  public void log(EsgWorker esgWorker, String s) {
    System.out.println(new Date() + "[" + esgWorker.getId() + "] " + s);
  }

  public void log(EsgWorker esgWorker, int rows) {
    int val = counter.addAndGet(rows);
    long end = System.currentTimeMillis();
    log(esgWorker,
        val + "/" + this.total + " rows in total, " + (double) (val * 100 / this.total)
            + "% finished! Speed: " + (val / ((end - start)/1000) ) + " rows/second");
  }
}
