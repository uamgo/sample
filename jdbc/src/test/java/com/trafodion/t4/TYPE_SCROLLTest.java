package com.trafodion.t4;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.Assert;

public class TYPE_SCROLLTest {
	protected static String url = "jdbc:t4jdbc://10.10.10.145:23400/:";
	protected static String driverClass = "org.trafodion.jdbc.t4.T4Driver";
	protected static String userName = "trafodion";
	protected static String pwd = "traf123";
	protected static String schema = "t4_test";
	private static final String table = "scroll_insert_test";
	protected static Connection conn;

	@Test
	public void cancelRow() throws SQLException {
		// insert a row
		String sql = "select * from " + schema + "." + table;
		Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		Object[] data = new Object[] { 1, "xiaoxin", 3.5, System.currentTimeMillis() };
		Object[] updatedData = new Object[] { 1, "xiaoxin2", 5.5, System.currentTimeMillis() + 500000000 };
		ResultSet rs = st.executeQuery(sql);
		rs.moveToInsertRow();
		rs.updateInt(1, (int) data[0]);
		rs.updateString(2, (String) data[1]);
		rs.updateDouble(3, (double) data[2]);
		rs.updateDate(4, new Date((long) data[3]));
		rs.insertRow();
		// Assert.assertTrue(rs.rowInserted());
		rs.close();

		// cancel update row
		rs = st.executeQuery(sql);
		Assert.assertTrue(rs.next());
		rs.updateString(2, (String) updatedData[1]);
		rs.updateDouble(3, (double) updatedData[2]);
		rs.updateDate(4, new Date((long) updatedData[3]));
		rs.cancelRowUpdates();
		Assert.assertTrue(!rs.rowUpdated());

		// update the row for refresh testing
		Statement up = conn.createStatement();
		up.execute("update " + schema + "." + table + " set name='xiaoxin_update' where id=1  ");
		up.close();

		Assert.assertEquals(data[1], rs.getString(2));
		rs.refreshRow();
		Assert.assertEquals(data[1] + "_update", rs.getString(2));

		// retrieves non updated row
		rs = st.executeQuery(sql);
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
		Assert.assertTrue(rs.next());
		Assert.assertEquals(data[0], rs.getInt(1));
		Assert.assertEquals(data[1] + "_update", rs.getString(2));
		Assert.assertEquals(data[2], rs.getDouble(3));
		Assert.assertEquals(f.format(new Date((long) data[3])), f.format(new Date(rs.getDate(4).getTime())));
		rs.close();

	}

	@Test
	public void insertRow() throws SQLException {
		// insert a row
		String sql = "select * from " + schema + "." + table;
		Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		Object[] data = new Object[] { 1, "xiaoxin", 3.5, System.currentTimeMillis() };
		Object[] updatedData = new Object[] { 1, "xiaoxin2", 5.5, System.currentTimeMillis() + 500000000 };
		ResultSet rs = st.executeQuery(sql);
		rs.moveToInsertRow();
		rs.updateInt(1, (int) data[0]);
		rs.updateString(2, (String) data[1]);
		rs.updateDouble(3, (double) data[2]);
		rs.updateDate(4, new Date((long) data[3]));
		rs.insertRow();
		// Assert.assertTrue(rs.rowInserted());
		rs.close();

		// retrieves inserted row
		rs = st.executeQuery(sql);
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
		Assert.assertTrue(rs.next());
		Assert.assertEquals(data[0], rs.getInt(1));
		Assert.assertEquals(data[1], rs.getString(2));
		Assert.assertEquals(data[2], rs.getDouble(3));
		Assert.assertEquals(f.format(new Date((long) data[3])), f.format(new Date(rs.getDate(4).getTime())));

		// update row
		rs.updateString(2, (String) updatedData[1]);
		rs.updateDouble(3, (double) updatedData[2]);
		rs.updateDate(4, new Date((long) updatedData[3]));
		rs.updateRow();
		Assert.assertTrue(rs.rowUpdated());
		rs.close();

		// check updated row
		// retrieves inserted row
		rs = st.executeQuery(sql);
		f = new SimpleDateFormat("yyyy-MM-dd");
		Assert.assertTrue(rs.next());
		Assert.assertEquals(updatedData[0], rs.getInt(1));
		Assert.assertEquals(updatedData[1], rs.getString(2));
		Assert.assertEquals(updatedData[2], rs.getDouble(3));
		Assert.assertEquals(f.format(new Date((long) updatedData[3])), f.format(new Date(rs.getDate(4).getTime())));

		rs.deleteRow();
		// Assert.assertTrue(rs.rowDeleted());
		rs.close();

		// check deleted row
		sql = "select count(1) from " + schema + "." + table;
		rs = st.executeQuery(sql);
		Assert.assertTrue(rs.next());
		Assert.assertEquals(rs.getInt(1), 0);
		rs.close();

	}

	@Before
	public void before() throws SQLException {
		try (Statement st = conn.createStatement()) {
			st.execute("delete from " + schema + "." + table);
		} finally {
		}
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		Class.forName(driverClass);
		conn = DriverManager.getConnection(url, userName, pwd);
		init();
	}

	public static void init() throws SQLException {
		try (Statement st = conn.createStatement()) {
			try {
				st.execute("create schema if not exists " + schema);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			try {
				st.execute("drop table if exists " + schema + "." + table);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			try {
				st.execute("create table if not exists " + schema + "." + table
						+ " (id int primary key  NOT NULL NOT DROPPABLE, name varchar(100), weight numeric(18,2), birthday timestamp)");
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		} catch (SQLException e) {
			throw e;
		}

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		conn.close();
	}
}
