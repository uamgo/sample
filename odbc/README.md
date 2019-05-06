##Example for ODBC drivers 
###Add DSN
```
odbcinst.ini
[Trafodion]
Description = ODBC for Trafodion
Driver      = {path_of_trafodion_odbc}/libtrafodbc_l64_drvr.so

odbc.ini
[dsn]
Driver = Trafodion
Server = TCP:localhost/23400
Schema = SEABASE
```
###To run the test case
```
g++ connect_test.cpp -lodbc
./a.out -d dsn -u uid -p pwd

```
###ODBC Sample 
######create a connection
```
SQLHENV henv = SQL_NULL_HANDLE;
SQLHDBC hdbc = SQL_NULL_HANDLE;
SQLHSTMT hstmt = SQL_NULL_HANDLE;
SQLHWND hWnd = SQL_NULL_HANDLE;
SQLAllocHandle(SQL_HANDLE_ENV, SQL_NULL_HANDLE, &henv);
SQLSetEnvAttr(henv, SQL_ATTR_ODBC_VERSION, (void*)SQL_OV_ODBC3, 0);
SQLAllocHandle(SQL_HANDLE_DBC, henv, &hdbc);
1. SQLDriverConnect(hdbc,hWnd,"DSN=%s;UID=%s;PWD=%s",SQL_NTS,OutConnStr,sizeof(OutConnStr),&ConnStrLength,SQL_DRIVER_NOPROMPT);
2. SQLConnect(hdbc, (SQLCHAR *)"dsn", SQL_NTS, (SQLCHAR *)"uid", SQL_NTS, (SQLCHAR *)"pwd", SQL_NTS);

```
######perform a simple query
```
SQLExecDirect(hstmt, (SQLCHAR *)"select * from t0", SQL_NTS);
while (SQLFetch(hstmt) == SQL_SUCCESS)
{
    char buffer[1024] = ""; 
    SQLGetData(hstmt, 1, SQL_C_CHAR, buffer, 1024, NULL);
    printf("%s\n", buffer);
}
SQLFreeHandle(SQL_HANDLE_STMT, hstmt);

```
######Batch insert
```
#define DESC_LEN 51  
#define ARRAY_SIZE 10  

SQLCHAR *      Statement = "INSERT INTO Parts (PartID, Description,  Price) "  
"VALUES (?, ?, ?)";  
SQLUINTEGER    PartIDArray[ARRAY_SIZE];  
SQLCHAR        DescArray[ARRAY_SIZE][DESC_LEN];  
SQLREAL        PriceArray[ARRAY_SIZE];  
SQLINTEGER     PartIDIndArray[ARRAY_SIZE], DescLenOrIndArray[ARRAY_SIZE],  
               PriceIndArray[ARRAY_SIZE];  
SQLUSMALLINT   i, ParamStatusArray[ARRAY_SIZE];  
SQLULEN ParamsProcessed;  

memset(DescLenOrIndArray, 0, sizeof(DescLenOrIndArray));  
memset(PartIDIndArray, 0, sizeof(PartIDIndArray));  
memset(PriceIndArray, 0, sizeof(PriceIndArray));  

// Set the SQL_ATTR_PARAM_BIND_TYPE statement attribute to use  
// column-wise binding.  
SQLSetStmtAttr(hstmt, SQL_ATTR_PARAM_BIND_TYPE, SQL_PARAM_BIND_BY_COLUMN, 0);  

// Specify the number of elements in each parameter array.  
SQLSetStmtAttr(hstmt, SQL_ATTR_PARAMSET_SIZE, ARRAY_SIZE, 0);  

// Specify an array in which to return the status of each set of  
// parameters.  
SQLSetStmtAttr(hstmt, SQL_ATTR_PARAM_STATUS_PTR, ParamStatusArray, 0);  

// Specify an SQLUINTEGER value in which to return the number of sets of  
// parameters processed.  
SQLSetStmtAttr(hstmt, SQL_ATTR_PARAMS_PROCESSED_PTR, &ParamsProcessed, 0);  

// Bind the parameters in column-wise fashion.  
SQLBindParameter(hstmt, 1, SQL_PARAM_INPUT, SQL_C_ULONG, SQL_INTEGER, 5, 0,  
        PartIDArray, 0, PartIDIndArray);  
SQLBindParameter(hstmt, 2, SQL_PARAM_INPUT, SQL_C_CHAR, SQL_CHAR, DESC_LEN - 1, 0,  
        DescArray, DESC_LEN, DescLenOrIndArray);  
SQLBindParameter(hstmt, 3, SQL_PARAM_INPUT, SQL_C_FLOAT, SQL_REAL, 7, 0,  
        PriceArray, 0, PriceIndArray);  

// Set part ID, description, and price.  
for (i = 0; i < ARRAY_SIZE; i++) {  
    GetNewValues(&PartIDArray[i], DescArray[i], &PriceArray[i]);  
    PartIDIndArray[i] = 0;  
    DescLenOrIndArray[i] = SQL_NTS;  
    PriceIndArray[i] = 0;  
}  

// Execute the statement.  
SQLExecDirect(hstmt, Statement, SQL_NTS);  

// Check to see which sets of parameters were processed successfully.  
for (i = 0; i < ParamsProcessed; i++) {  
    printf("Parameter Set  Status\n");  
    printf("-------------  -------------\n");  
    switch (ParamStatusArray[i]) {  
        case SQL_PARAM_SUCCESS:  
        case SQL_PARAM_SUCCESS_WITH_INFO:  
            printf("%13d  Success\n", i);  
            break;  

        case SQL_PARAM_ERROR:  
            printf("%13d  Error\n", i);  
            break;  

        case SQL_PARAM_UNUSED:  
            printf("%13d  Not processed\n", i);  
            break;  

        case SQL_PARAM_DIAG_UNAVAILABLE:  
            printf("%13d  Unknown\n", i);  
            break;  

    }  
}  

```
######release connection
```
SQLDisconnect(hdbc);
SQLFreeHandle(SQL_HANDLE_DBC, hdbc);
SQLFreeHandle(SQL_HANDLE_ENV, henv);

```
