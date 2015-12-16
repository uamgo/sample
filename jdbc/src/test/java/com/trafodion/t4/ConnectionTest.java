package com.trafodion.t4;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trafodion.common.BaseTest;

public class ConnectionTest extends BaseTest {
	private Logger log = LoggerFactory.getLogger(ConnectionTest.class);
	@Test
	public void create() throws SQLException, ClassNotFoundException {
		Class.forName(driverClass);
		for (int i = 0; i < 1000; i++) {
			System.out.println(i);
			conn = DriverManager.getConnection(url, userName, pwd);
			Statement st = conn.createStatement();
			if(st.execute("get schemas")){
				ResultSet rs = st.getResultSet();
				while(rs.next()){
					log.info(rs.getObject(1)+"");
				}
			}
			conn.close();
		}
	}

	@BeforeClass
	public static void beforeClass() throws Exception {

	}

	@AfterClass
	public static void cleanup() throws Exception {

	}
}
