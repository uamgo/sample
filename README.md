##Example for JDBC/ODBC/ADO.NET drivers
###JDBC Sample
```
url: <jdbc:t4jdbc://10.10.10.136:23400/:>    
driver class: org.trafodion.jdbc.t4.T4Driver
```
######create a connection
```
Class.forName(driverClass);  
Connection conn = DriverManager.getConnection(url, userName, pwd);
```
######create a Statement
```
Statement st = conn.createStatement();
```
######perform a query
```
ResultSet rs = st.executeQuery("select * from t1");  
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
