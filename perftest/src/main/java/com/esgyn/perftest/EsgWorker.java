package com.esgyn.perftest;

import java.sql.*;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

public class EsgWorker extends Thread {
  private final int id;
  private final String sql;
  private final int batchSize;
  private final String url;
  private final String user;
  private final String pwd;
  private final int rows;
  private final List<EsgColumn> columnList;
  private final Random r;
  private final EsgStats stats;

  public EsgWorker(int id, List<EsgColumn> columnList, Properties conf, EsgStats stats, int rows) {
    this.id = id;
    this.sql = conf.getProperty("sql");
    this.batchSize = (int) conf.get("batch");
    this.url = conf.getProperty("url");
    this.user = conf.getProperty("user");
    this.pwd = conf.getProperty("pwd");
    this.rows = rows;
    this.columnList = columnList;
    this.r = new Random();
    this.stats = stats;
  }

  @Override public void run() {
    this.stats
        .log(this, "thread is starting. rows:" + this.rows + ", batch size:" + this.batchSize);
    Connection conn = null;
    PreparedStatement ps = null;
    long start = 0;
    try {
      Object o = null;
      int x = 0;
      conn = DriverManager.getConnection(this.url, this.user, this.pwd);
      ps = conn.prepareStatement(this.sql);
      start = System.currentTimeMillis();
      for (int n = 0; n < this.rows; n++) {
        for (int i = 1; i <= this.columnList.size(); i++) {
          o = genValue(this.columnList.get(i - 1));
          ps.setObject(i, o);
        }
        ps.addBatch();
        ++x;
//        System.out.println(x + ": " + this.batchSize);
        if (x >= this.batchSize) {
          try {
            ps.executeBatch();
          }catch (Exception e){
            e.printStackTrace();
            this.stats.log(this, "Skip "+ x +" rows");
          }
          this.stats.log(this, x);
          x = 0;
        }
      }
      if (x > 0) {
        try {
          ps.executeBatch();
        }catch (Exception e){
          e.printStackTrace();
          this.stats.log(this, "Skip "+ x +" rows");
        }
        this.stats.log(this, x);
      }
    } catch (BatchUpdateException be) {
      SQLException se = be;
      se.printStackTrace();
      se = be.getNextException();
      if (se != null) {
        se.printStackTrace();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (ps != null) {
        try {
          ps.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
      long end = System.currentTimeMillis();
      this.stats.log(this,
          "thread finished! Speed: " + (this.rows / ((end - start) / 1000)) + " rows/second.");
    }
  }

  private Object genValue(EsgColumn esgColumn) throws Exception {
    switch (esgColumn.getType()) {
    case Types.BIGINT:
      return r.nextLong();
    case Types.VARCHAR:
      int n = Math.abs(r.nextInt(esgColumn.getLen() / 4));
      if (n == 0) {
        n = 1;
      }
      String s = this.id + this.r.nextInt(10000) + UUID.randomUUID().toString() + this.r.nextInt();
      if(esgColumn.isPk()){
        if(s.length()> esgColumn.getLen()) s=s.substring(0,esgColumn.getLen()-1);
      }else
      if (s.length() > n) s = s.substring(0, n);
      return s;
    case Types.INTEGER:
      return r.nextInt();
    case Types.TIMESTAMP:
      return new Timestamp(System.currentTimeMillis());
    case Types.CHAR:
      return "1";
    default:
      throw new Exception("Not supported: " + esgColumn);
    }

  }
}
