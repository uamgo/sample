package com.trafodion.t4;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.junit.Test;

import com.trafodion.common.BaseTest;

public class InsertTest extends BaseTest {
	protected static String url = "jdbc:t4jdbc://10.10.11.3:23400/:connectionTimeout=0";

	@Test
	public void prepareTest() throws SQLException {
		PreparedStatement ps = conn.prepareStatement(
				"INSERT INTO seabase.OSD28DL_Z1 (SK_PE9ETUK,JQJAAFUO4B,O4BQVTFXOS,D28DL_Z1HP,EK7XIK0RPU,LR5GT3YMEY,CWBGHNZMVN,SWI9CJQJAA,FUO4BQVTFX,QJZVF2ABMN,QIGXW1HCDO,VOWHPAC75R,L6DYIXRZJ3,L0MSNYUGTF,B8SK_PE9ET,UKQJZVF2AB,MN4QIGXW1H,CDOVOWHPAC,RL6DYIXRZJ,QVTFXOSD28,DL_Z1HPEK7,XIK0RPULR5,GT3YMEYCWB,GHNZMVN6SW,I9CJQJAAFU) "
						+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		for (int i = 1; i <= 25; i++) {
			ps.setObject(i, null);
		}
		ps.execute();
		ps.close();
	}

	@Test
	public void insert() throws SQLException {
		String sql = "INSERT INTO TEMP_ROOT_NEW" +
				"(ROOT, POLICYNO, OLDPOLICYNO, PROPOSALNO, INSERTTIMEFORHIS,ETLSOURCEFLAG)" +
				" SELECT DISTINCT r.OLDPOLICYNO ROOT,r.POLICYNO,r.OLDPOLICYNO,r.PROPOSALNO,r.INSERTTIMEFORHIS,r.ETLSOURCEFLAG"
				+ " FROM CXC_PRPCRENEWAL_INC i inner join CXC_PRPCRENEWAL r "
				+ "on i.OLDPOLICYNO=r.POLICYNO and i.POLICYNO is not null "
				+ "and NOT EXISTS (SELECT 1 FROM TEMP_ROOT_NEW ii WHERE ii.POLICYNO=r.POLICYNO and ii.POLICYNO IS NOT NULL)";
		String incSql = "INSERT INTO TEMP_ROOT_NEW" +
				"(ROOT, POLICYNO, OLDPOLICYNO, PROPOSALNO, INSERTTIMEFORHIS,ETLSOURCEFLAG) "
				+ " SELECT DISTINCT OLDPOLICYNO ROOT,POLICYNO,OLDPOLICYNO,PROPOSALNO,INSERTTIMEFORHIS,ETLSOURCEFLAG "
				+ " FROM CXC_PRPCRENEWAL_INC";
		System.out.println(incSql);
		Statement st = conn.createStatement();
//		st.execute("set schema picc");
		int r = st.executeUpdate(incSql);
		System.out.println("result:" + r);
	}

	@Test
	public void insert2() throws SQLException {
		long t = System.currentTimeMillis();
		String sql = "SELECT DISTINCT R.OLDPOLICYNO ROOT,R.POLICYNO,R.OLDPOLICYNO,R.PROPOSALNO,R.INSERTTIMEFORHIS,R.ETLSOURCEFLAG, 0  "
				+ "FROM CXC_PRPCRENEWAL R WHERE R.OLDPOLICYNO in (SELECT DISTINCT NVL(OLDPOLICYNO,POLICYNO) FROM CXC_PRPCRENEWAL_INC) "
				+ "AND  NOT EXISTS (SELECT 1 FROM CXC_PRPCRENEWAL_INC I WHERE R.OLDPOLICYNO=I.POLICYNO AND I.OLDPOLICYNO IS NOT NULL)";
		Statement st = conn.createStatement();
		st.execute("set schema picc");
		ResultSet rs = st.executeQuery(sql);
		PreparedStatement ps = conn.prepareStatement(
				"upsert using load into TEMP_ROOT_NEW(ROOT, POLICYNO, OLDPOLICYNO, PROPOSALNO, INSERTTIMEFORHIS,ETLSOURCEFLAG, lvl) values(?,?,?,?,?,?,?)");
		long num = insert(rs, ps);
		System.out.println("done! " + num);
		recurse(conn);
		System.out.println("all done!" + (System.currentTimeMillis() - t) / 1000);
	}

	public static void recurse(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		st.execute("select T.ROOT,R.POLICYNO,R.OLDPOLICYNO,R.PROPOSALNO,R.INSERTTIMEFORHIS,R.ETLSOURCEFLAG, ? " +
				"from TEMP_ROOT_NEW T join CXC_PRPCRENEWAL R " +
				"  on T.POLICYNO=R.OLDPOLICYNO " +
				"where T.lvl = ?");

		PreparedStatement ps = conn.prepareStatement(
				"upsert using load into TEMP_ROOT_NEW(ROOT, POLICYNO, OLDPOLICYNO, PROPOSALNO, INSERTTIMEFORHIS,ETLSOURCEFLAG, lvl) values(?,?,?,?,?,?,?)");

		int lvl = 0;
		long numRowsUpdated = 0;
		ResultSet rs = null;
		do {
			rs = st.executeQuery(String.format(
					"select T.ROOT,R.POLICYNO,R.OLDPOLICYNO,R.PROPOSALNO,R.INSERTTIMEFORHIS,R.ETLSOURCEFLAG, %d " +
							"from TEMP_ROOT_NEW T join CXC_PRPCRENEWAL R " +
							"  on T.POLICYNO=R.OLDPOLICYNO " +
							"where T.lvl = %d",
					lvl + 1, lvl));
			numRowsUpdated=insert(rs, ps);
			rs.close();
			lvl++;
		} while (numRowsUpdated > 0);
		ps.close();
	}

	public static long insert(ResultSet rs, PreparedStatement ps) throws SQLException {
		try{
		int n = 0;
		int batchSize = 10000;
		long num = 0;
		while (rs.next()) {
			ps.setObject(1, rs.getObject(1));
			ps.setObject(2, rs.getObject(2));
			ps.setObject(3, rs.getObject(3));
			ps.setObject(4, rs.getObject(4));
			ps.setObject(5, rs.getObject(5));
			ps.setObject(6, rs.getObject(6));
			ps.setObject(7, rs.getObject(7));
			ps.addBatch();
			num++;
			if (++n > batchSize) {
				ps.executeBatch();
				System.out.println("n: " + n);
				n = 0;
			}
		}
		if (n > 0) {
			ps.executeBatch();
		}
		return num;
		}catch(SQLException e){
			Iterator<Throwable> it = e.iterator();
			while(it.hasNext()){
				it.next().printStackTrace();
			}
			throw e;
		}
	}
}
