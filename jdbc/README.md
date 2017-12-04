##Example for JDBC/ODBC/ADO.NET drivers
###JDBC Sample
######create a connection
```
String url="jdbc:t4jdbc://10.10.10.136:23400/:";    
String driverClass="org.trafodion.jdbc.t4.T4Driver";
String userName="zz";
String pwd="zz";
Class.forName(driverClass);  //load driver class
Connection conn = DriverManager.getConnection(url, userName, pwd); //get a new connection
```
######perform a simple query
```
Statement st = conn.createStatement();  //create a statement
ResultSet rs = st.executeQuery("select * from t1"); //perform a query
while (rs.next()) {  
  System.out.println(rs.getInt(1) + "," + rs.getString(2) + "," + rs.getDouble(3));  
}
//release ResultSet and Statement
rs.close();  
st.close();  

```
######Batch insert
```
try {
    PreparedStatement ps = conn.prepareStatement("upsert using load into mylocaltest.t1(id, name, weight) values(?,?,?)");
    for (int i = 0; i < 1000; i++) {
        ps.setInt(1, r.nextInt());
        ps.setString(2, "aaa" + r.nextLong());
        ps.setDouble(1, r.nextDouble());
        ps.addBatch();
    }
    ps.executeBatch();
//close your statement
} catch (SQLException se) {
//print details error messages for each row
    SQLException s = se;
    do {
        s.printStackTrace();
        s = s.getNextException();
    } while (s != null);
} finally {
    if (ps != null)
        ps.close();
}
```
######release connection
```
conn.close();
```
######Apply a connection pool on Trafodion JDBC driver. Take [HikariCP](https://github.com/brettwooldridge/HikariCP) as an example
```
//related HikariCP packages
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

//create a connection pool. The pool usually is a global variable.
HikariConfig config = new HikariConfig();
config.setMaximumPoolSize(10);
config.setIdleTimeout(60000);
config.setDriverClassName("org.trafodion.jdbc.t4.T4Driver");
config.setJdbcUrl("jdbc:t4jdbc://192.168.0.34:23400/:");
config.setUsername("zz");
config.setPassword("zz");
pool = new HikariDataSource(config);

//get a connection from connection pool
Connection conn = pool.getConnection("org.trafodion.jdbc.t4.T4Driver");
/*
doing what you want with conn
*/
conn.close();//conn will return back to pool
```
