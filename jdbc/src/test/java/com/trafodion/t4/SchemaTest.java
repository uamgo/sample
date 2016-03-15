package com.trafodion.t4;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trafodion.common.BaseTest;

public class SchemaTest extends BaseTest {
	private static Logger log = LoggerFactory.getLogger(SchemaTest.class);

	@Test
	public void getTables() throws SQLException {
		DatabaseMetaData md;
		md = conn.getMetaData();
		ResultSet rs = md.getTableTypes();
		while (rs.next()) {
			System.out.println(rs.getObject(1));
		}
		rs.close();

		ResultSet tables = md.getTables("TRAFODION", "SEABASE", "%", new String[] { "TABLE" });
		while (tables.next()) {
			System.out.println(tables.getObject(1));
		}

		tables.close();
	}
	
	@Test
	public void getProcedureColumns() throws SQLException {
		DatabaseMetaData md;
		md = conn.getMetaData();
		ResultSet p = md.getProcedureColumns("TRAFODION", "DB__LIBMGR", "PUT", "%");
		ResultSetMetaData rsmd = p.getMetaData();
		for (int i = 1; i <= rsmd.getColumnCount(); i++)
			System.out.print(rsmd.getColumnName(i) + ", ");
		System.out.println();
		while (p.next()) {
			System.out.println("---------");
			for (int i = 1; i <= rsmd.getColumnCount(); i++)
				System.out.print(p.getObject(i) + "["+i+"], ");
		}
		p.close();
	}

	@Test
	public void getProcedures() throws SQLException {
		DatabaseMetaData md;
		md = conn.getMetaData();
		ResultSet p = md.getProcedures("TRAFODION", "DB_LIBMGR", "%");
		ResultSetMetaData rsmd = p.getMetaData();
		for (int i = 1; i <= rsmd.getColumnCount(); i++)
			System.out.print(rsmd.getColumnName(i) + ", ");
		System.out.println();
		while (p.next()) {
			for (int i = 1; i <= rsmd.getColumnCount(); i++)
				System.out.print(p.getObject(i) + ", ");
			System.out.println();
		}
		p.close();
	}

	@Test
	public void getIndexs() throws SQLException {
		DatabaseMetaData md;
		md = conn.getMetaData();
		ResultSet indexes = md.getIndexInfo("%", "%", "%", false, true);
		ResultSetMetaData rsmd = indexes.getMetaData();
		while (indexes.next()) {
			for (int i = 1; i <= rsmd.getColumnCount(); i++)
				System.out.print(indexes.getObject(i)+", ");
			System.out.println();
		}

		indexes.close();
	}

	@Test
	public void getTypeInfo() {
		try {
			DatabaseMetaData md;
			md = conn.getMetaData();
			ResultSet s = md.getTypeInfo();
			boolean flag = false;
			ResultSetMetaData rsmd = s.getMetaData();
			log.info("Column count: " + rsmd.getColumnCount());
			StringBuilder sb = new StringBuilder();
			while (s.next()) {
				sb.setLength(0);
				for (int i = 1; i <= rsmd.getColumnCount(); i++)
					sb.append(s.getObject(i) + ", ");
				log.info(sb.toString());
				flag = true;
			}
			Assert.assertTrue(flag);

			md = conn.getMetaData();
			s = md.getTypeInfo();
			flag = false;
			rsmd = s.getMetaData();
			log.info("Column count: " + rsmd.getColumnCount());
			while (s.next()) {
				sb.setLength(0);
				for (int i = 1; i <= rsmd.getColumnCount(); i++)
					sb.append(s.getObject(i) + ", ");
				log.info(sb.toString());
				flag = true;
			}
			Assert.assertTrue(flag);
		} catch (SQLException e) {
			log.error(e.getSQLState(), e);
			e.printStackTrace();
		}
	}

	@Test
	public void showControlAll() throws Exception {
		Statement st = conn.createStatement();
		boolean hasrs = st.execute("showcontrol all");
		Assert.assertTrue(hasrs);
		ResultSet rs = st.getResultSet();
		while (rs.next()) {
			log.info(rs.getObject(1) + "");
		}
		rs.close();
		st.close();

	}

	@Test
	public void getSchemasTest() {
		try {
			DatabaseMetaData md;
			md = conn.getMetaData();
			ResultSet s = md.getSchemas();
			boolean flag = false;
			ResultSetMetaData rsmd = s.getMetaData();
			log.info("Column count: " + rsmd.getColumnCount());
			StringBuilder sb = new StringBuilder();
			while (s.next()) {
				sb.setLength(0);
				for (int i = 1; i <= rsmd.getColumnCount(); i++)
					sb.append(s.getObject(i) + ", ");
				log.info(sb.toString());
				flag = true;
			}
			Assert.assertTrue(flag);
		} catch (SQLException e) {
			log.error(e.getSQLState(), e);
			e.printStackTrace();
		}
	}

	@Test
	public void getCatalogsTest() {
		try {
			DatabaseMetaData md;
			md = conn.getMetaData();
			ResultSet s = md.getCatalogs();
			boolean flag = false;
			ResultSetMetaData rsmd = s.getMetaData();
			log.info("Column count: " + rsmd.getColumnCount());
			StringBuilder sb = new StringBuilder();
			while (s.next()) {
				sb.setLength(0);
				for (int i = 1; i <= rsmd.getColumnCount(); i++)
					sb.append(s.getObject(i) + ", ");
				log.info(sb.toString());
				flag = true;
			}
			Assert.assertTrue(flag);
		} catch (SQLException e) {
			log.error(e.getSQLState(), e);
			e.printStackTrace();
		}

	}

	public static void prepareData() throws Exception {
		log.info("SchemaTest prepareData");
	}
}
