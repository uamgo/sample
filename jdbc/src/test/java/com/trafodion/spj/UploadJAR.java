package com.trafodion.spj;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import com.trafodion.common.BaseTest;

public class UploadJAR extends BaseTest {
	private static final Logger log = LoggerFactory.getLogger(UploadJAR.class);
	private static final String CHARTSET = "ISO-8859-1";

	@Test
	public void download() throws SQLException, IOException {
		String name = "ojdbc14-10.2.0.4.0.jar";
		CallableStatement pc = this.conn.prepareCall("{call default_spj.GETFILE(?,?,?,?)}");
		int offset = 0;
		pc.registerOutParameter(3, Types.BIGINT);
		pc.registerOutParameter(4, Types.VARCHAR);
		String data = null;
		byte[] unData = null;
		long len = 1;
		FileOutputStream out = new FileOutputStream("D:\\lib\\" + name + ".test.jar");
		while (offset < len) {
			pc.setString(1, name);
			pc.setInt(2, offset);
			pc.execute();
			data = pc.getString(3);
			System.out.println("data size:" + data.length());
			unData = Snappy.uncompress(data.getBytes(CHARTSET));
			out.write(unData);
			len = pc.getLong(4);
			offset += unData.length;
			log.info("offset=" + offset + ",len=" + len);
			System.out.println("offset=" + offset + ",len=" + len);
			if (len == 1) {
				break;
			}
		}
		out.close();
		pc.close();
	}

	@Test
	public void upload() throws SQLException, IOException {
		CallableStatement pc = this.conn.prepareCall("{call default_spj.put(?,?,?)}");
		File file = new File("d:\\lib\\ojdbc14-10.2.0.4.0.jar");
		FileInputStream in = new FileInputStream(file);
		byte[] b = new byte[102400];
		int len = -1;
		int flag = 1;
		int clen = 0;
		while ((len = in.read(b)) != -1) {
			log.info("file length: " + len);
			String s = new String(b, 0, len, "ISO-8859-1");
			clen = Snappy.compress(s.getBytes("ISO-8859-1")).length;
			log.info("converted length: " + s.getBytes("ISO-8859-1").length);
			log.info("compressed length: " + clen);
			pc.setString(1, new String(Snappy.compress(s.getBytes("ISO-8859-1")), "ISO-8859-1"));
			pc.setString(2, file.getName());
			pc.setInt(3, flag);
			pc.execute();
			if (flag == 1) {
				flag = 0;
			}
		}
		in.close();
		pc.close();

		// pc.setString(1, x);
	}

	@Test
	public void uploadPartially() throws SQLException, IOException {
		CallableStatement pc = this.conn.prepareCall("{call default_spj.put(?,?,?,?)}");
		File file = new File("d:\\lib\\slf4j-api-1.7.12.jar");
		FileInputStream in = new FileInputStream(file);
		byte[] b = new byte[10240];
		int len = -1;
		int flag = 1;
		int clen = 0;
		while ((len = in.read(b)) != -1) {
			log.info("file length: " + len);
			String s = new String(b, 0, len, "ISO-8859-1");
			clen = Snappy.compress(s.getBytes("ISO-8859-1")).length;
			log.info("converted length: " + s.getBytes("ISO-8859-1").length);
			log.info("compressed length: " + clen);
			pc.setString(1, new String(Snappy.compress(s.getBytes("ISO-8859-1")), "ISO-8859-1"));
			pc.setString(2, file.getName());
			pc.setInt(3, clen);
			pc.setInt(4, flag);
			pc.execute();
			if (flag == 1) {
				flag = 0;
			}
		}
		in.close();
		pc.close();

		// pc.setString(1, x);
	}

}
