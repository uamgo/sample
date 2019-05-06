#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <sql.h>
#include <sqlext.h>

SQLHENV henv;
SQLHDBC hdbc;
SQLHSTMT hstmt;
SQLHWND hWnd;

#define MAX_SQLSTRING_LEN	1000
#define STATE_SIZE		6
#define MAX_CONNECT_STRING      256
#define TRUE			1
#define FALSE			0
#define	ARGS			"d:u:p:"
#define STR_LEN 128 + 1  
#define REM_LEN 254 + 1

const char *SqlRetText(int rc)
{
	static char buffer[80];
	switch (rc)
	{
	case SQL_SUCCESS:
		return("SQL_SUCCESS");
	case SQL_SUCCESS_WITH_INFO:
		return("SQL_SUCCESS_WITH_INFO");
	case SQL_NO_DATA:
		return("SQL_NO_DATA");
	case SQL_ERROR:
		return("SQL_ERROR");
	case SQL_INVALID_HANDLE:
		return("SQL_INVALID_HANDLE");
	case SQL_STILL_EXECUTING:
		return("SQL_STILL_EXECUTING");
	case SQL_NEED_DATA:
		return("SQL_NEED_DATA");
	}
	sprintf(buffer,"SQL Error %d",rc);
	return(buffer);
}

void CleanUp()
{
	printf("\nConnect Test FAILED!!!\n");
	if(hstmt != SQL_NULL_HANDLE)
		SQLFreeHandle(SQL_HANDLE_STMT,hstmt);
	if(hdbc != SQL_NULL_HANDLE)
	{
		SQLDisconnect(hdbc);
		SQLFreeHandle(SQL_HANDLE_DBC,hdbc);
	}
	if(henv != SQL_NULL_HANDLE)
		SQLFreeHandle(SQL_HANDLE_ENV,henv);
	exit(EXIT_FAILURE);

}

void LogDiagnostics(const char *sqlFunction, SQLRETURN rc, bool exitOnError=true)
{             
	SQLRETURN diagRC = SQL_SUCCESS;
	SQLSMALLINT recordNumber;
	SQLINTEGER nativeError, diagColumn, diagRow;
	SQLCHAR messageText[SQL_MAX_MESSAGE_LENGTH];
	SQLCHAR sqlState[6];
	int diagsPrinted = 0;
	bool printedErrorLogHeader = false;
	
	printf("Function %s returned %s\n", sqlFunction, SqlRetText(rc));

	/* Log any henv Diagnostics */
	recordNumber = 1;
	do{
		diagRC = SQLGetDiagRec(SQL_HANDLE_ENV, henv, recordNumber,sqlState,&nativeError,messageText,sizeof(messageText),NULL);
		if(diagRC==SQL_SUCCESS)
		{
			if(!printedErrorLogHeader){
				printf("Diagnostics associated with environment handle:\n");
				printedErrorLogHeader = true;
			}
			printf("\n\tSQL Diag %d\n\tNative Error: %ld\n\tSQL State:    %s\n\tMessage:      %s\n",
				recordNumber,nativeError,sqlState,messageText);
		}
		recordNumber++;
	} while (diagRC==SQL_SUCCESS);
	
   /* Log any hdbc Diagnostics */
	recordNumber = 1;
	printedErrorLogHeader = false;
	do{
		diagRC = SQLGetDiagRec(SQL_HANDLE_DBC, hdbc, recordNumber,sqlState,&nativeError,messageText,sizeof(messageText),NULL);
		if(diagRC==SQL_SUCCESS)
		{
			if(!printedErrorLogHeader){
				printf("Diagnostics associated with connection handle:\n");
				printedErrorLogHeader = true;
			}
			printf("\n\tSQL Diag %d\n\tNative Error: %ld\n\tSQL State:    %s\n\tMessage:      %s\n",
				recordNumber,nativeError,sqlState,messageText);
		}
		recordNumber++;
	} while (diagRC==SQL_SUCCESS);

   /* Log any hstmt Diagnostics */
	recordNumber = 1;
	printedErrorLogHeader = false;
	do{
		diagRC = SQLGetDiagRec(SQL_HANDLE_STMT, hstmt, recordNumber,sqlState,&nativeError,messageText,sizeof(messageText),NULL);
		if(diagRC==SQL_SUCCESS)
		{
			if(!printedErrorLogHeader){
				printf("Diagnostics associated with statmement handle:\n");
				printedErrorLogHeader = true;
			}
			printf("\n\tSQL Diag %d\n\tNative Error: %ld\n\tSQL State:    %s\n\tMessage:      %s\n",
				recordNumber,nativeError,sqlState,messageText);
		}
		recordNumber++;
	} while (diagRC==SQL_SUCCESS);

	if(exitOnError && rc!=SQL_SUCCESS_WITH_INFO)
		CleanUp();
}                     

// Main Program
int main (int argc, char **argv)
{
	SQLCHAR		*dsnName;
	SQLCHAR		*user;
	SQLCHAR		*password;
	SQLRETURN	returnCode;
	bool		testPassed = true;
	SQLCHAR		InConnStr[MAX_CONNECT_STRING];
	SQLCHAR		OutConnStr[MAX_CONNECT_STRING];
	SQLSMALLINT	ConnStrLength;
	int c, errflag = 0;
	
	optarg = NULL;
	if (argc != 7)
		errflag++;

	while (!errflag && (c = getopt(argc, argv, ARGS)) != -1)
		switch (c) {
			case 'd':
				dsnName = (SQLCHAR*)optarg;	
				break;
			case 'u':
				user = (SQLCHAR*)optarg;
				break;
			case 'p':
				password = (SQLCHAR*)optarg;
				break;
			default :
				errflag++;
		}
	if (errflag) {
		printf("Command line error.\n");
		printf("Usage: %s [-d <datasource>] [-u <userid>] [-p <password>]\n", argv[0] );
		return FALSE;
	}

	// Initialize handles to NULL
	henv = SQL_NULL_HANDLE;
	hstmt = SQL_NULL_HANDLE;
	hdbc = SQL_NULL_HANDLE;

	// Allocate Environment Handle
	returnCode = SQLAllocHandle(SQL_HANDLE_ENV, SQL_NULL_HANDLE, &henv);
	if(returnCode != SQL_SUCCESS)
		LogDiagnostics("SQLAllocHandle(SQL_HANDLE_ENV, SQL_NULL_HANDLE, &henv)",returnCode);

	// Set ODBC version to 3.0
	returnCode = SQLSetEnvAttr(henv, SQL_ATTR_ODBC_VERSION, (void*)SQL_OV_ODBC3, 0); 
	if(returnCode != SQL_SUCCESS)
		LogDiagnostics("SQLSetEnvAttr(henv, SQL_ATTR_ODBC_VERSION, (void*)SQL_OV_ODBC3, 0)",returnCode,false);

	// Allocate Connection handle
	returnCode = SQLAllocHandle(SQL_HANDLE_DBC, henv, &hdbc);
	if(returnCode != SQL_SUCCESS)
		LogDiagnostics("SQLAllocHandle(SQL_HANDLE_DBC, henv, &hdbc)", returnCode);

	//Connect to the database
	sprintf((char*)InConnStr,"DSN=%s;UID=%s;PWD=%s;%c",(char*)dsnName, (char*)user, (char*)password,'\0');
	printf("Using Connect String: %s\n", InConnStr);
	returnCode = SQLDriverConnect(hdbc,hWnd,InConnStr,SQL_NTS,OutConnStr,sizeof(OutConnStr),&ConnStrLength,SQL_DRIVER_NOPROMPT);
	if(returnCode != SQL_SUCCESS)
		LogDiagnostics("SQLDriverConnect",returnCode);

	//Allocate Statement handle
	returnCode = SQLAllocHandle(SQL_HANDLE_STMT, hdbc, &hstmt);
	if(returnCode != SQL_SUCCESS)
		LogDiagnostics("SQLAllocHandle(SQL_HANDLE_STMT, hdbc, &hstmt)", returnCode);

    returnCode = SQLColumns(hstmt, (SQLCHAR *)"TRAFODION", SQL_NTS, (SQLCHAR *)"SEABASE", SQL_NTS, (SQLCHAR *)"LOBTEST", SQL_NTS , (SQLCHAR *)"%", SQL_NTS);
	if(returnCode != SQL_SUCCESS)
		LogDiagnostics("SQLColumns", returnCode);

    SQLCHAR szSchema[STR_LEN] = "";
    SQLCHAR szCatalog[STR_LEN] = "";
    SQLCHAR szColumnName[STR_LEN] = ""; 
    SQLCHAR szTableName[STR_LEN] = "";
    SQLCHAR szTypeName[STR_LEN] = "";
    SQLCHAR szRemarks[REM_LEN] = "";
    SQLCHAR szColumnDefault[STR_LEN] = "";
    SQLCHAR szIsNullable[STR_LEN] = "";

    SQLINTEGER ColumnSize;  
    SQLINTEGER BufferLength;  
    SQLINTEGER CharOctetLength;  
    SQLINTEGER OrdinalPosition;  

    SQLSMALLINT DataType;  
    SQLSMALLINT DecimalDigits;  
    SQLSMALLINT NumPrecRadix;  
    SQLSMALLINT Nullable;  
    SQLSMALLINT SQLDataType;  
    SQLSMALLINT DatetimeSubtypeCode;

    SQLLEN cbCatalog;  
    SQLLEN cbSchema;  
    SQLLEN cbTableName;  
    SQLLEN cbColumnName;  
    SQLLEN cbDataType;  
    SQLLEN cbTypeName;  
    SQLLEN cbColumnSize;  
    SQLLEN cbBufferLength;  
    SQLLEN cbDecimalDigits;  
    SQLLEN cbNumPrecRadix;  
    SQLLEN cbNullable;  
    SQLLEN cbRemarks;  
    SQLLEN cbColumnDefault;  
    SQLLEN cbSQLDataType;  
    SQLLEN cbDatetimeSubtypeCode;  
    SQLLEN cbCharOctetLength;  
    SQLLEN cbOrdinalPosition;  
    SQLLEN cbIsNullable;

    if (returnCode == SQL_SUCCESS || returnCode == SQL_SUCCESS_WITH_INFO) {
        SQLBindCol(hstmt, 1, SQL_C_CHAR, szCatalog, STR_LEN,&cbCatalog);  
        SQLBindCol(hstmt, 2, SQL_C_CHAR, szSchema, STR_LEN, &cbSchema);  
        SQLBindCol(hstmt, 3, SQL_C_CHAR, szTableName, STR_LEN,&cbTableName);  
        SQLBindCol(hstmt, 4, SQL_C_CHAR, szColumnName, STR_LEN, &cbColumnName);  
        SQLBindCol(hstmt, 5, SQL_C_SSHORT, &DataType, 0, &cbDataType);  
        SQLBindCol(hstmt, 6, SQL_C_CHAR, szTypeName, STR_LEN, &cbTypeName);  
        SQLBindCol(hstmt, 7, SQL_C_SLONG, &ColumnSize, 0, &cbColumnSize);  
        SQLBindCol(hstmt, 8, SQL_C_SLONG, &BufferLength, 0, &cbBufferLength);  
        SQLBindCol(hstmt, 9, SQL_C_SSHORT, &DecimalDigits, 0, &cbDecimalDigits);  
        SQLBindCol(hstmt, 10, SQL_C_SSHORT, &NumPrecRadix, 0, &cbNumPrecRadix);  
        SQLBindCol(hstmt, 11, SQL_C_SSHORT, &Nullable, 0, &cbNullable);  
        SQLBindCol(hstmt, 12, SQL_C_CHAR, szRemarks, REM_LEN, &cbRemarks);  
        SQLBindCol(hstmt, 13, SQL_C_CHAR, szColumnDefault, STR_LEN, &cbColumnDefault);  
        SQLBindCol(hstmt, 14, SQL_C_SSHORT, &SQLDataType, 0, &cbSQLDataType);  
        SQLBindCol(hstmt, 15, SQL_C_SSHORT, &DatetimeSubtypeCode, 0, &cbDatetimeSubtypeCode);  
        SQLBindCol(hstmt, 16, SQL_C_SLONG, &CharOctetLength, 0, &cbCharOctetLength);  
        SQLBindCol(hstmt, 17, SQL_C_SLONG, &OrdinalPosition, 0, &cbOrdinalPosition);  
        SQLBindCol(hstmt, 18, SQL_C_CHAR, szIsNullable, STR_LEN, &cbIsNullable);
        while (SQL_SUCCESS == returnCode) {
            returnCode = SQLFetch(hstmt);
            if (returnCode == SQL_SUCCESS || returnCode == SQL_SUCCESS_WITH_INFO)
            {
                printf("%s.%s.%s column%d: %s, type = %d, typeName = %s, columnSize = %d, length = %d, decimal = %d, sqlType = %d\n", szCatalog, szSchema, szTableName, OrdinalPosition, szColumnName, DataType, szTypeName, ColumnSize, BufferLength, DecimalDigits, SQLDataType);
            }
        }
    }

	//Free Statement handle
	returnCode = SQLFreeHandle(SQL_HANDLE_STMT, hstmt);
	if(returnCode != SQL_SUCCESS)
		LogDiagnostics("SQLFreeHandle(SQL_HANDLE_STMT, hstmt)", returnCode);
	hstmt = SQL_NULL_HANDLE;

	//Disconnect
	returnCode = SQLDisconnect(hdbc);
	if(returnCode != SQL_SUCCESS)
		LogDiagnostics("SQLDisconnect(hdbc)", returnCode);

	//Free Connection handle
	returnCode = SQLFreeHandle(SQL_HANDLE_DBC, hdbc);
	if(returnCode != SQL_SUCCESS)
		LogDiagnostics("SQLFreeHandle(SQL_HANDLE_DBC, hdbc)", returnCode);
	hdbc = SQL_NULL_HANDLE;

	//Free Environment handle
	returnCode = SQLFreeHandle(SQL_HANDLE_ENV, henv);
	if(returnCode != SQL_SUCCESS)
		LogDiagnostics("SQLFreeHandle(SQL_HANDLE_ENV, henv)", returnCode);
	henv = SQL_NULL_HANDLE;

	printf("\nConnect Test Passed...\n");
	exit(EXIT_SUCCESS);
}


