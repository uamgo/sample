package org.apache.hive.hplsql;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

public class Proc {
	private final static String plCreateDDL = "CREATE TABLE IF NOT EXISTS %s(PL_NAME VARCHAR(500) NOT NULL NOT DROPPABLE PRIMARY KEY, PL_TEXT VARCHAR(102400))";
	private final static String plQueryString = "SELECT pl_text FROM %s";
	private final static String ENCODER = "UTF-8";

	/**
	 * @param content
	 * @param mdTableName
	 * @param errMsg
	 */
	public static void exec(String content, String mdTableName, String[] errMsg) {

	}

	/**
	 * @param file
	 * @param mdTableName
	 * @param clazzName
	 * @param url
	 * @param user
	 * @param pwd
	 */
	public static void exec(Connection conn, File file, String mdTableName, String clazzName, String url, String user,
			String pwd) {
		Statement st = null;
		ResultSet rs = null;
		StringBuilder log = new StringBuilder();
		try {
			if (mdTableName == null) {
				mdTableName = "seabase.PROC_MD_TAB";
			}
			Properties config = new Properties();

			config.put("hplsql.conn.default", "trafconn");
			config.put("hplsql.conn.trafconn",
					clazzName + ";" + url + ";" + user + ";" + pwd);

			st = conn.createStatement();
			checkTable(st, mdTableName);
			rs = st.executeQuery(String.format(plQueryString, mdTableName));
			StringBuilder sb = new StringBuilder();
			boolean hasProc = false;
			while (rs.next()) {
				sb.append(rs.getString(1) + "\n");
				hasProc = true;
			}
			// ByteArrayOutputStream bao = new ByteArrayOutputStream();
			// PrintStream b = new PrintStream(bao, true, ENCODER);
			//
			// System.setOut(b);
			// System.setErr(System.out);
			Exec exec = new Exec(config);
			if(hasProc)
				exec.run(new String[] { "-e", sb.toString(), "-trace", "-offline" });

			String[] args = { "-f", file.getAbsolutePath(), "-trace" };
			exec.run(args, exec.function);
			exec.cleanup(true);
			// log.append(new String(bao.toByteArray(), ENCODER) + "\n");

		} catch (Exception e) {
			log.append(e.getMessage() + "\n");
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	private static void checkTable(Statement st, String mdTableName) throws SQLException {
		String sql = "create table if not exists trafodion.seabase.proc_md_tab("
				+ "pl_name varchar(500) character set iso88591 collate default no default not null not droppable serialized,"
				+ "pl_text varchar(102400) character set iso88591 collate default default null serialized,"
				+ "primary key (pl_name asc))";
		st.execute(sql);
		// st.execute(String.format(plCreateDDL, mdTableName));
	}

	public static void main(String[] args) throws IOException, Exception {
		File logName = null;
		try {
			PrintStream o = System.out;
			System.setErr(o);
			String fileName = "D:\\final_raise.sql";
			String content = null;
			if (args.length > 0) {
				content = args[0];
				// logName = new File(args[1]);
			} else {
				content = FileUtils.readFileToString(new java.io.File(fileName), "UTF-8");
			}
			System.out.println(content);

			String[] result = new String[1];
			String url = "jdbc:t4jdbc://192.168.0.45:23400/:";
			String driverClass = "org.trafodion.jdbc.t4.T4Driver";
			Class.forName(driverClass);
			Connection conn = DriverManager.getConnection(url, "trafodion", "traf123");
			exec(conn, new File(fileName), null, driverClass, url, "trafodion", "traf123");

			System.setOut(o);
			System.setErr(System.out);
			// FileUtils.write(logName, "-------------\n");
			// FileUtils.write(logName, result[0] + "\n");
			System.out.println("-----------------");
			System.out.println(result[0]);
		} catch (IOException e) {
			// FileUtils.write(logName, e.getMessage()+"\n");
			e.printStackTrace();
		}
	}

}
