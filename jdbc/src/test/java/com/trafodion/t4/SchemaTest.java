package com.trafodion.t4;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trafodion.common.BaseTest;

public class SchemaTest extends BaseTest {
	private Logger log = LoggerFactory.getLogger(SchemaTest.class);

	@Test
	public void getSchemasTest() {
		try {
			DatabaseMetaData md;
			md = conn.getMetaData();
			ResultSet s = md.getSchemas();
			while (s.next()) {
				log.info("--"+s.getObject(1) + "");
			}
		} catch (SQLException e) {
			log.error(e.getSQLState(),e);
			e.printStackTrace();
		}
	}

}
