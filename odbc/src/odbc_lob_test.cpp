#include <stdio.h>
#include <sqlext.h>
#include <sql.h>
#include <string.h>

void daig(SQLSMALLINT handleType, SQLHANDLE handle)
{
    SQLRETURN retcode;
    SQLTCHAR   sqlState[6];
    SQLINTEGER nativeError;
    SQLTCHAR    messageBuf[1024];
    SQLSMALLINT availableLen = SQL_NTS;

    SQLSMALLINT i = 1;
    retcode = SQLGetDiagRec(handleType, handle, i, sqlState, &nativeError, messageBuf, 512, &availableLen);
    while (SQL_SUCCEEDED(retcode))
    {
        printf("[%s][%d][%s]\n", sqlState, nativeError, messageBuf);
        ++i;
        retcode = SQLGetDiagRec(handleType, handle, i, sqlState, &nativeError, messageBuf, 512, &availableLen);
    }
}

int check(SQLRETURN ret, char *func, SQLSMALLINT handleType, SQLHANDLE handle)
{
    if (SQL_SUCCEEDED(ret))
    {
        printf("%s SUCESS\n", func);
        return 0;
    }
    else
    {
        printf("%s FAILED\n", func);
        daig(handleType, handle);
        return -1;
    }
}

int main(int argc, CHAR* argv[])
{
    SQLHANDLE henv, hdbc, hstmt;
    SQLRETURN retcode;
    char Outstr[1024];
    SQLSMALLINT len;

    retcode = SQLAllocEnv(&henv);
    retcode = SQLSetEnvAttr(henv, SQL_ATTR_ODBC_VERSION, (SQLPOINTER)SQL_OV_ODBC3, SQL_NTS);
    retcode = SQLAllocConnect(henv, &hdbc);
    retcode = SQLConnect(hdbc, (SQLCHAR *)"dsn", SQL_NTS, (SQLCHAR *)"uid", SQL_NTS, (SQLCHAR *)"pwd", SQL_NTS);
    check(retcode, "SQLConnect", SQL_HANDLE_DBC, hdbc);
    retcode = SQLAllocStmt(hdbc, &hstmt);

#if 1
    //create table
    retcode = SQLExecDirect(hstmt, (SQLCHAR *)"cqd traf_blob_as_varchar 'off'", SQL_NTS);
    check(retcode, "CQD off", SQL_HANDLE_STMT, hstmt);
    retcode = SQLExecDirect(hstmt, (SQLCHAR *)"create table lobtest(a int, b blob)", SQL_NTS);
    check(retcode, "create table", SQL_HANDLE_STMT, hstmt);
#endif 

#if 1
    //insert data
    retcode = SQLPrepare(hstmt, (SQLCHAR *)"insert into lobtest values(1, ?)", SQL_NTS);
    check(retcode, "SQLPrepare", SQL_HANDLE_STMT, hstmt);

    char value[] = "aaaaaaaaaaaaa";
    SQLLEN datalen = sizeof(value); //data size should be given

    retcode = SQLBindParameter(hstmt, 1, SQL_PARAM_INPUT_OUTPUT, SQL_C_CHAR, SQL_VARCHAR, 0, 0, value, sizeof(value), &datalen);
    check(retcode, "SQLBindParameter", SQL_HANDLE_STMT, hstmt);

    retcode = SQLExecute(hstmt);
    check(retcode, "SQLExecute", SQL_HANDLE_STMT, hstmt);
#endif

#if 1
    //insert data larger than 16M with update
    //1. select and get the lob handle
    retcode = SQLExecDirect(hstmt, (SQLCHAR *)"select b from lobtest where a = 1", SQL_NTS);
    check(retcode, "select", SQL_HANDLE_STMT, hstmt);

    retcode = SQLFetch(hstmt);
    check(retcode, "SQLFetch", SQL_HANDLE_STMT, hstmt);
    char datavalue[102400] = "";
    while (retcode == SQL_SUCCESS)
    {
        retcode = SQLGetData(hstmt, 1, SQL_C_CHAR, datavalue, 102400, NULL);
        check(retcode, "SQLGetData", SQL_HANDLE_STMT, hstmt);

        printf("origin data = %s\n", datavalue);
        retcode = SQLFetch(hstmt);
    }

    //2. execute "LOBUPDATE"
    SQLFreeHandle(SQL_HANDLE_STMT, hstmt);
    SQLAllocStmt(hdbc, &hstmt);
    retcode = SQLExecDirect(hstmt, (SQLCHAR *)"LOBUPDATE", SQL_NTS);
    if (retcode == SQL_NEED_DATA)
        printf("lob update success\n");
    else if (retcode == SQL_ERROR)
        check(retcode, "lob update", SQL_HANDLE_STMT, hstmt);
    else
        printf("lob update failed , rc = %d\n", retcode);

    SQLPOINTER pParmID;
    retcode = SQLParamData(hstmt, &pParmID);
    if (retcode == SQL_NEED_DATA) {
        char value1[] = "bbbbbbbbbb";
        char value2[] = "cccccccccc";
        printf("SQLParamData 1 success\n");

        retcode = SQLPutData(hstmt, value1, strlen(value1));
        check(retcode, "SQLPutData 1", SQL_HANDLE_STMT, hstmt);

        retcode = SQLPutData(hstmt, value2, strlen(value2));
        check(retcode, "SQLPutData 2", SQL_HANDLE_STMT, hstmt);
    }
    else
        printf("SQLParamData 1 failed , rc = %d\n", retcode);

    retcode = SQLParamData(hstmt, &pParmID);
    check(retcode, "SQLParamData", SQL_HANDLE_STMT, hstmt);
#endif

#if 1
    //check the data
    retcode = SQLExecDirect(hstmt, (SQLCHAR *)"select b from lobtest where a = 1", SQL_NTS);
    check(retcode, "select", SQL_HANDLE_STMT, hstmt);

    retcode = SQLFetch(hstmt);
    check(retcode, "SQLFetch", SQL_HANDLE_STMT, hstmt);

    memset(datavalue, 0, sizeof(datavalue));

    retcode = SQLGetData(hstmt, 1, SQL_C_CHAR, datavalue, 1024, NULL);
    check(retcode, "SQLGetData", SQL_HANDLE_STMT, hstmt);

    printf("after data = %s\n", datavalue);
#endif

    getchar();

    return 0;
}
