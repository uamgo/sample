package com.esgyn.perftest;

import org.apache.commons.cli.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class EsgRunner {

  public static void main(String[] args) throws Exception {
    CommandLine line = parserCommandLine(args);
    run(line);
  }

  private static void run(CommandLine line) throws Exception {
    Class.forName("org.trafodion.jdbc.t4.T4Driver");
    Properties conf = new Properties();
    String ip = line.getOptionValue("i");
    String port = line.getOptionValue("p", "23400");
    String user = line.getOptionValue("u");
    String pwd = line.getOptionValue("w");
    String table = line.getOptionValue("t");
    String opts = line.getOptionValue("o", "");
    int threads = Integer.valueOf(line.getOptionValue("e", "5"));
    int batch = Integer.valueOf(line.getOptionValue("b", "100"));
    int rows = Integer.valueOf(line.getOptionValue("r", "10000"));

    String url = "jdbc:t4jdbc://" + ip + ":" + port + "/:" + opts;
    conf.put("url", url);
    conf.put("user", user);
    conf.put("pwd", pwd);
    conf.put("table", table);
    conf.put("batch", batch);
    conf.put("total", rows);
    List<EsgColumn> columnList = checkTable(conf);
    int n = 0;
    if (columnList.size() > 0) {
      n = rows / threads;
    }
    String sql = "upsert using load into " + table + " values(" + String
        .join(",", Collections.nCopies(columnList.size(), "?")) + ")";
    conf.put("sql", sql);
    System.out.println(sql);
    EsgStats stats = new EsgStats(conf);
    for (int i = 0; i < threads; i++) {
      if (i == (threads - 1)) {
        new EsgWorker(i, columnList, conf, stats, (rows - i * n)).start();
      } else {
        new EsgWorker(i, columnList, conf, stats, n).start();
      }
    }

  }

  private static List<EsgColumn> checkTable(Properties conf) throws SQLException {

    String url = conf.getProperty("url");
    String user = conf.getProperty("user");
    String pwd = conf.getProperty("pwd");
    String[] table = conf.getProperty("table").split("[.]");
    String catalog = "TRAFODION";
    String schema = "SEABASE";
    String t = "";
    if (table.length == 1) {
      t = table[0];
    } else if (table.length == 2) {
      schema = table[0];
      t = table[1];

    } else if (table.length == 3) {
      catalog = table[0];
      schema = table[1];
      t = table[2];
    } else {
      System.out.println(conf.getProperty("table") + ", with len:" + table.length);
    }

    Connection conn = null;
    ResultSet rs = null;
    ResultSet pkrs = null;
    try {
      conn = DriverManager.getConnection(url, user, pwd);
      DatabaseMetaData md = conn.getMetaData();
      pkrs = md.getPrimaryKeys(catalog, schema, t);
      List<String> pks = new ArrayList<>();
      while (pkrs.next()) {
        pks.add(pkrs.getString("COLUMN_NAME"));
      }

      rs = md.getColumns(catalog, schema, t, null);
      List<EsgColumn> cols = new ArrayList<EsgColumn>();
      ResultSetMetaData rsmd = rs.getMetaData();
      EsgColumn esgColumn = null;
      String colName = null;
      while (rs.next()) {
        colName = rs.getString("COLUMN_NAME");
        esgColumn = new EsgColumn(colName, rs.getInt("DATA_TYPE"), rs.getInt("COLUMN_SIZE"));
        if (pks.contains(colName)) {
          esgColumn.setPk(true);
        }
        cols.add(esgColumn);

//        System.out.println(String
//            .format("%s, Type: %d, Size: %d", rs.getString("COLUMN_NAME"), rs.getInt("DATA_TYPE"),
//                rs.getInt("COLUMN_SIZE")));
        //        String colString = "";
        //        for (int i = 0; i < rsmd.getColumnCount(); i++) {
        //          colString += rs.getObject(i + 1) + ", ";
        //        }
        //        System.out.println(colString);
      }
      return cols;
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static CommandLine parserCommandLine(String[] args) throws ParseException {
    CommandLineParser parser = new DefaultParser();
    // create the Options
    Options options = new Options();
    options.addOption("h", "help", false, "Usage:");
    options.addOption("i", "ip", true, "Database IP address for testing.");
    options.addOption("p", "port", true, "Database port for testing, 23400 by default.");
    options.addOption("u", "user", true, "Database user name for testing.");
    options.addOption("w", "pwd", true, "Database password for testing.");
    options.addOption("t", "table", true, "Table name with  for testing.");
    options.addOption("o", "props", true,
        "Database url properties for testing, for example: catalog=trafodion;schema=seabase. "
            + "Use catalog=trafodion\\;schema=seabase on terminal.");
    options.addOption("e", "threads", true, "The number of threads for testing. 5 by default.");
    options.addOption("b", "batch", true, "Batch size for testing. 100 by default.");
    options.addOption("r", "rows", true, "total rows for testing. 10000 by default.");

    CommandLine line = parser.parse(options, args);
    if (line.hasOption("h")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("options", options);
      System.exit(0);
    }
    return line;
  }
}
