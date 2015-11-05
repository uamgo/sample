##Example for JDBC/ODBC/ADO.NET drivers
###JDBC Sample
```
String url="jdbc:t4jdbc://10.10.10.136:23400/:";    
String driverClass="org.trafodion.jdbc.t4.T4Driver";
String userName="zz";
String pwd="zz";
```
######create a connection and perform a simple query
```
Class.forName(driverClass);  //load driver class
Connection conn = DriverManager.getConnection(url, userName, pwd); //get a new connection
Statement st = conn.createStatement();  //create a statement
ResultSet rs = st.executeQuery("select * from t1"); //perform a query
while (rs.next()) {  
  System.out.println(rs.getInt(1) + "," + rs.getString(2) + "," + rs.getDouble(3));  
}
```
######release connection
```
rs.close();  
st.close();  
conn.close();
```
