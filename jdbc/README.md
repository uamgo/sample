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
PreparedStatement ps = conn.prepareStatement("insert into mylocaltest.t1(id, name, weight) values(?,?,?)");
for(int i=0; i<1000; i++){
	ps.setInt(1, r.nextInt());
	ps.setString(2, "aaa"+r.nextLong());
	ps.setDouble(1, r.nextDouble());
	ps.addBatch();
}
ps.executeBatch();
//close your statement
ps.close();
```
######release connection
```
conn.close();
```
