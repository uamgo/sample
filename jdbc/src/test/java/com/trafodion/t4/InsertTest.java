package com.trafodion.t4;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.Test;

import com.trafodion.common.BaseTest;

public class InsertTest extends BaseTest {

	@Test
	public void prepareTest() throws SQLException {
		PreparedStatement ps = conn.prepareStatement(
				"INSERT INTO seabase.OSD28DL_Z1 (SK_PE9ETUK,JQJAAFUO4B,O4BQVTFXOS,D28DL_Z1HP,EK7XIK0RPU,LR5GT3YMEY,CWBGHNZMVN,SWI9CJQJAA,FUO4BQVTFX,QJZVF2ABMN,QIGXW1HCDO,VOWHPAC75R,L6DYIXRZJ3,L0MSNYUGTF,B8SK_PE9ET,UKQJZVF2AB,MN4QIGXW1H,CDOVOWHPAC,RL6DYIXRZJ,QVTFXOSD28,DL_Z1HPEK7,XIK0RPULR5,GT3YMEYCWB,GHNZMVN6SW,I9CJQJAAFU) "
						+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		for(int i=1; i<=25;i++){
			ps.setObject(i, null);
		}
		ps.execute();
		ps.close();
		
		
	}
}
