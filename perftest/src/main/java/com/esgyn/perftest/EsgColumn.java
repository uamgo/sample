package com.esgyn.perftest;

import java.sql.Types;

public class EsgColumn {

  private String name;
  private int len;
  private int type;
  private boolean pk;

  public EsgColumn(String name, int type, int len) {
    this.name = name;
    this.type = type;
    this.len = len;
  }

  public int getType() {
    return this.type;
  }

  public int getLen() {
    return len;
  }

  @Override public String toString() {
    return "EsgColumn{" + "name='" + name + '\'' + ", len=" + len + ", type=" + type + '}';
  }

  public void setPk(boolean pk) {
    this.pk = pk;
  }

  public boolean isPk() {
    return pk;
  }
}
