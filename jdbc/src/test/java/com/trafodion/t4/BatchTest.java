package com.trafodion.t4;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trafodion.common.BaseTest;

public class BatchTest extends BaseTest {
	private static final Logger log = LoggerFactory.getLogger(BatchTest.class);

	@Test
	public void insertTest() throws SQLException{
		String create = "create table if not exists O4BQVTFXOS(QJAAFUO4BQ int,VTFXOSD28D int) NO PARTITION ";
		Statement st = conn.createStatement();
		st.execute(create);
		st.close();
		PreparedStatement ps = conn.prepareStatement("insert into O4BQVTFXOS values (?, ?)");
		ps.setInt(1, 100);
		ps.setInt(2, 200);
		ps.addBatch();
		ps.executeBatch();
		ps.clearBatch();
		ps.close();
		ps = conn.prepareStatement("select * from O4BQVTFXOS where QJAAFUO4BQ = ? or VTFXOSD28D = ? ");
		ps.setInt(1, 100);
		ps.setInt(2, 200);
		ps.execute();
		ResultSet rs = ps.getResultSet();
		while(rs.next()){
			log.info("---"+rs.getObject(1)+","+rs.getObject(2));
		}
		rs.close();
		ps.close();
	}
	
	@Test
	public void result() throws SQLException {
		try {
			String sql = "insert into mylocaltest.t1 values(?,?,?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			for (int i = 0; i < 10; i++) {
				// if (i == 7) {
				// ps.setLong(1, Long.MAX_VALUE);
				// } else
				ps.setInt(1, i);
				if (i != 5) {
					ps.setString(2,
							"ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss");
				} else
					ps.setString(2, "sss");
				ps.setFloat(3, 3.3F);
				ps.addBatch();
			}
			int[] rs = ps.executeBatch();
			for (int i = 0; i < rs.length; i++) {
				if (rs[i] < 0 && rs[i] != -2)
					log.info("Error/warning on row " + i);
			}
			SQLWarning warn = ps.getWarnings();
			while (warn != null) {
				log.info("Error " + warn.getMessage());
				warn = warn.getNextWarning();
			}
			ps.close();
			log.info("select ...");
			Statement st = conn.createStatement();
			ResultSet res = st.executeQuery("select * from mylocaltest.t1");
			while (res.next()) {
				log.info(res.getObject(1) + "," + res.getObject(2) + "," + res.getObject(3));
			}
			st.close();
		} catch (SQLException e) {
			SQLException ie = e;
			while (ie != null) {
//				if(ie instanceof HPT4Exception)
				log.error(ie.getMessage());
				ie = ie.getNextException();
			}

		}
	}
}
