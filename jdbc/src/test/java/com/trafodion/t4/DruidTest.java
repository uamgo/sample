package com.trafodion.t4;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.trafodion.common.BaseTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DruidTest {
  protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);
  protected static String url = "jdbc:t4jdbc://192.168.0.79:23400/:";
  protected static String driverClass = "org.trafodion.jdbc.t4.T4Driver";
  protected static String userName = "db__root";
  protected static String pwd = "traf123";
  private static DruidDataSource dataSource = null;

  @BeforeClass public static void beforeClass() throws ClassNotFoundException, SQLException {
    dataSource = new DruidDataSource();
    dataSource.setDbType("oracle");
    dataSource.setInitialSize(3);
    dataSource.setMaxActive(20);
    //should go with the changes in T4Driver.getMajorVerion > 10
    dataSource.setFilters("wall,stat");
    dataSource.setDriverClassName(driverClass);
    dataSource.setUrl(url);
    dataSource.setUsername(userName);
    dataSource.setPassword(pwd);
  }

  @Test public void checkTrafT4() throws SQLException {
    DruidPooledConnection conn = dataSource.getConnection();
    Statement st = conn.createStatement();
    ResultSet rs = st.executeQuery("select COUNT(1) from dual");
    while (rs.next()) {
      System.out.println("RS: " + rs.getObject(1));
    }
    rs.close();
    st.close();
    conn.close();
    dataSource.close();
  }

}
