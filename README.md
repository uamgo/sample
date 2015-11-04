##Example for JDBC/ODBC/ADO.NET drivers
###JDBC Sample
<code>
url: <jdbc:t4jdbc://10.10.10.136:23400/:><br/>
driver class: org.trafodion.jdbc.t4.T4Driver
</code>
######create a connection
<code>
Class.forName(driverClass);<br/>
Connection conn = DriverManager.getConnection(url, userName, pwd);
</code>
######create a Statement
<code>
Statement st = conn.createStatement();
</code>
######perform a query
<code>
ResultSet rs = st.executeQuery("select * from t1");<br/>
while (rs.next()) {<br/>
&nbsp;&nbsp;System.out.println(rs.getInt(1) + "," + rs.getString(2) + "," + rs.getDouble(3));<br/>
			}
</code>
######release connection
<code>
rs.close();<br/>
st.close();<br/>
conn.close();
</code>
