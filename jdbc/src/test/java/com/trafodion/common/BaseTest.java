package com.trafodion.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseTest {
	protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);
	protected static String url = "jdbc:t4jdbc://10.10.10.169:23400/:";
	protected static String driverClass = "org.trafodion.jdbc.t4.T4Driver";
	protected static String userName = "trafodion";
	protected static String pwd = "traf123";
	protected static Connection conn;

	@BeforeClass
	public static void beforeClass() throws Exception {
		Class.forName(driverClass);
		log.info("connecting...");
		conn = DriverManager.getConnection(url, userName, pwd);
		log.info("connected");
		//prepareData();
	}

	public static void prepareData() throws Exception {
		Random r = new Random();
		log.info("Preparing data...");
		Statement st = conn.createStatement();
		log.info("created statement");
		st.execute("create schema if not exists mylocaltest");
		log.info("created schema mylocaltest");
		st.execute("create table if not exists mylocaltest.t1 (id int, name varchar(20),weight numeric(18,2))");
		log.info("created table mylocaltest.t1");
		st.execute("insert into mylocaltest.t1 values(" + r.nextInt() + ",'aaa'," + r.nextFloat() + "),"
				+ "(" + r.nextInt() + ",'aaa'," + r.nextFloat() + "),"
				+ "(" + r.nextInt() + ",'aaa'," + r.nextFloat() + ")");
		log.info("inserted into mylocaltest.t1 3 rows");
		st.execute("insert into mylocaltest.t1 select * from mylocaltest.t1");
		st.close();
		log.info("6 rows has been inserted into mylocaltest.t1");
	}

	@AfterClass
	public static void cleanup() throws Exception {
		try {
			Statement st = conn.createStatement();
			st.execute("drop schema if exists mylocaltest cascade");
			st.close();
		} catch (Exception e) {
			System.err.println("Warning: " + e.getMessage());
		}
		if (conn != null) {
			conn.close();
		}
	}

	protected static void close(Statement st, ResultSet rs) throws Exception {
		try {
			if (rs != null)
				rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (st != null) {
			st.close();
		}
	}
}
