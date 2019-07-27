package com.trafodion.t4;

import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.*;

public class OracleTest {
  private static Connection conn = null;

  @BeforeClass public static void beforeClase() throws SQLException {
    String url = "jdbc:oracle:thin:@//172.31.231.169/ORCL";
    //    String user = "db__root";
    String user = "trafodion";
    String pwd = "traf123";
    String driver = "oracle.jdbc.driver.OracleDriver";
    conn = DriverManager.getConnection(url, user, pwd);

  }

  @Test public void savepoint() throws SQLException {
    String insert = "insert into t1 values(1,'aaa')";
    String spName = "aaaaaaaaaa";
    conn.setAutoCommit(false);
    Savepoint sp = conn.setSavepoint(spName);
    Statement st = conn.createStatement();
    st.execute(insert);
    conn.rollback(sp);
    st.execute(insert);
    conn.rollback(sp);
    conn.close();
  }
}
