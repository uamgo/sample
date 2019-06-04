##Example for JDBC/ODBC/ADO.NET drivers
###JDBC Sample( For more details, please go into "jdbc")
```
String url="jdbc:t4jdbc://10.10.10.136:23400/:schema=seabase";    
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
###ADO.NET Driver Sample( For more details, please go into "ado")  
######create a connection and perform a simple query
```
EsgynDBConnection conn = new EsgynDBConnection(); //create a connection
conn.ConnectionString = "server=10.0.0.5:23400;user=zz;password=zz;schema=ado";
conn.Open();
EsgynDBCommand cmd = conn.CreateCommand(); //create a command
cmd.CommandText = "select * from t0";
using (EsgynDBDataReader dr = cmd.ExecuteReader())
{
    while (dr.Read())
    {
	    for (int i = 0; i < dr.FieldCount; i++)
	    {
	      Console.Write(dr.GetValue(i) + " " + dr.GetDataTypeName(i));
	     }

	     Console.Write("\r\n");
    }
}
```
###ODBC Sample( For more details, please go into "odbc") 
######create a connection and perform a simple query  
https://docs.microsoft.com/en-us/sql/odbc/reference/develop-app/developing-applications?view=sqlallproducts-allversions  

```
SQLHENV henv = SQL_NULL_HANDLE;
SQLHDBC hdbc = SQL_NULL_HANDLE;
SQLHSTMT hstmt = SQL_NULL_HANDLE;
SQLHWND hWnd = SQL_NULL_HANDLE;
SQLAllocHandle(SQL_HANDLE_ENV, SQL_NULL_HANDLE, &henv);
SQLSetEnvAttr(henv, SQL_ATTR_ODBC_VERSION, (void*)SQL_OV_ODBC3, 0);
SQLAllocHandle(SQL_HANDLE_DBC, henv, &hdbc);
SQLDriverConnect(hdbc,hWnd,InConnStr,SQL_NTS,OutConnStr,sizeof(OutConnStr),&ConnStrLength,SQL_DRIVER_NOPROMPT);
SQLAllocHandle(SQL_HANDLE_STMT, hdbc, &hstmt);
SQLExecDirect(hstmt, (SQLCHAR *)"select * from t0", SQL_NTS);
while (SQLFetch(hstmt) == SQL_SUCCESS)
{
    char buffer[1024] = "";
    SQLGetData(hstmt, 1, SQL_C_CHAR, buffer, 1024, NULL);
    printf("%s\n", buffer);
}
SQLFreeHandle(SQL_HANDLE_STMT, hstmt);
SQLDisconnect(hdbc);
SQLFreeHandle(SQL_HANDLE_DBC, hdbc);
SQLFreeHandle(SQL_HANDLE_ENV, henv);
```
