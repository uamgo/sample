package com.trafodion.t4;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trafodion.common.BaseTest;

public class SelectTest extends BaseTest {
	private static final Logger log = LoggerFactory.getLogger(SelectTest.class);

	@Test
	public void exec2in1() throws SQLException{
		String sql = "get tables;select * from \"_MD_\".objects limit 1;";
		conn.createStatement().execute(sql);
	}
	
	@Test
	public void executeTwice() throws SQLException{
		 String query = "SELECT * FROM t1 WHERE id=? AND name=?";
	        conn.setAutoCommit(true);
	        try {
	            PreparedStatement statement = conn.prepareStatement(query);
	            statement.setString(1, "1");
	            statement.setString(2, "1");
	            ResultSet rs = statement.executeQuery();
	            assertTrue(rs.next());
	            assertFalse(rs.next());
	            rs.close();
	            // Run another query through same connection and make sure
	            // you can't find the new row
	            rs = statement.executeQuery();
	            assertTrue(rs.next());
	        } finally {
	        }
	}
	
	@Test
	public void executeTwice2() throws SQLException{
		 String query = "SELECT * FROM \"_MD_\".objects WHERE object_type=?";
	        conn.setAutoCommit(true);
	        try {
	            PreparedStatement statement = conn.prepareStatement(query);
	            statement.setString(1, "LB");
	            ResultSet rs = statement.executeQuery();
	            assertTrue(rs.next());
//	            rs.close();
	            // Run another query through same connection and make sure
	            // you can't find the new row
	            ResultSet rs2 = statement.executeQuery();
	            assertTrue(rs2.next());
	            boolean r1 = false;
	            boolean r2 = false;
	            while((r1=rs.next())
	            		){
//	            		||(r2 = rs2.next())){
	            	if(r1)
	            	System.out.println(rs.getObject(1));
//	            	if(r2)
//	            		System.out.println(rs2.getObject(1));
	            }
	        } finally {
	        }
	}
	
	@Test
	public void afterLastRow() throws Exception {
		Statement st = null;
		ResultSet rs = null;
		try {
			String sql = "select W_NAME, W_STREET_1, W_STREET_2, W_CITY, W_STATE, W_ZIP from JAVABENCH.OE_WAREHOUSE_32 where W_ID=2";
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			ResultSetMetaData md = rs.getMetaData();
			while (rs.next()) {
				for (int i = 1; i <= md.getColumnCount(); i++)
					log.info(rs.getObject(i) + "");
			}
		} finally {
			if (st != null) {
				st.close();
			}
		}
		
		log.info("---------------------------");
		PreparedStatement ps = null;
		try {
			String sql = "select W_NAME, W_STREET_1, W_STREET_2, W_CITY, W_STATE, W_ZIP from JAVABENCH.OE_WAREHOUSE_32 where W_ID=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, 2);
			rs = ps.executeQuery();
			ResultSetMetaData md = rs.getMetaData();
			while (rs.next()) {
				for (int i = 1; i <= md.getColumnCount(); i++)
					log.info(rs.getObject(i) + "");
			}

		} finally {
			if (ps != null) {
				ps.close();
			}
		}
		
	}

	@Test
	public void select1() throws Exception {
		String sql = "select * from mylocaltest.t1";
		Statement st = null;
		ResultSet rs = null;
		try {
			st = conn.createStatement();
			ResultSet su = st.executeQuery("values(session_user)");
			while (su.next()) {
				System.out.println(su.getObject(1));
			}
			su.close();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				log.info(rs.getInt(1) + "," + rs.getString(2) + "," + rs.getDouble(3));
			}
		} finally {
			close(st, rs);
		}

	}

	@Test
	public void select2() throws Exception {
		String sql = "select * from mylocaltest.t1";
		Statement st = null;
		ResultSet rs = null;
		try {
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			ResultSetMetaData md = rs.getMetaData();
			log.info("print all data:");
			System.out.println();
			while (rs.next()) {
				for (int i = 0; i < md.getColumnCount(); i++) {
					if (i > 0) {
						System.out.print(",");
					}
					System.out.print(md.getColumnName(i + 1) + ":" + rs.getObject(i + 1));
				}
				System.out.println();
			}
		} finally {
			close(st, rs);
		}

	}

}
