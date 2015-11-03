package com.trafodion.t4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class LoadDataTest {
	private static String fromUrl = "jdbc:t4jdbc://10.10.10.136:23400/:";
	private static String fromDriverClass = "org.trafodion.jdbc.t4.T4Driver";
	private static String fromUserName = "traf123";
	private static String fromPwd = "aaa";

	private static String toUrl = "jdbc:t4jdbc://10.10.10.136:23400/:";
	private static String toDriverClass = "org.trafodion.jdbc.t4.T4Driver";
	private static String toUserName = "traf456";
	private static String toPwd = "bbb";
	private static Connection fromConn;
	private static Connection toConn;

	@BeforeClass
	public static void beforeClass() throws Exception {
		Class.forName(fromDriverClass);
		Class.forName(fromDriverClass);
		fromConn = DriverManager.getConnection(fromUrl, fromUserName, fromPwd);
		toConn = DriverManager.getConnection(toUrl, toUserName, toPwd);
		prepareData();
	}

	public void loading(){
		String fromSql = "";
		
	}
	
	
	
	
	private static void close(Statement st, ResultSet rs) throws Exception {
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
	private static void prepareData() throws Exception {
		Random r = new Random();
		Statement st = fromConn.createStatement();
		st.execute("create schema if not exists mylocaltest");
		st.execute("create table if not exists mylocaltest.t1 (id int, name varchar(100),weight numeric(18,2))");
		st.execute("create table if not exists mylocaltest.t2 (id int, name varchar(100),weight numeric(18,2))");
		st.close();
		PreparedStatement ps = fromConn.prepareStatement("insert into mylocaltest.t1");
		for(int i=0; i<1000; i++){
			ps.setInt(1, r.nextInt());
			ps.setString(2, "aaa"+r.nextLong());
			ps.setDouble(1, r.nextDouble());
			ps.addBatch();
		}
		ps.executeBatch();
		System.out.println("6 rows has been inserted into mylocaltest.t1");
	}
	@AfterClass
	public static void cleanup() throws Exception {
		try {
			Statement st = fromConn.createStatement();
			st.execute("drop schema if exists mylocaltest cascade");
			st.close();
		} catch (Exception e) {
			System.err.println("Warning: " + e.getMessage());
		}
		if (fromConn != null) {
			fromConn.close();
		}
		if (toConn != null) {
			toConn.close();
		}
	}

}
