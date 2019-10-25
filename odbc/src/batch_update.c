#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <sql.h>
#include <sqlext.h>
#include <sqltypes.h>
#include <sqlext.h>
#ifdef unixcli
#include <ctype.h>
#else
#include <windows.h>
#include <tchar.h>
#endif
#include <assert.h>

#ifndef unixcli

#ifndef TCHAR
#ifdef UNICODE
#define TCHAR   WCHAR
#else
#define TCHAR   char
#endif /*UNICODE*/
#endif /*TCHAR*/

#endif /*unixcli*/

#define CONNECT_AUTOCOMMIT_OFF        0x01    // Default is on.
#define CONNECT_ODBC_VERSION_2        0x02
#define CONNECT_ODBC_VERSION_3        0x04

#define NONE            0x00     /* Don't use any options.  Equivalent to using LogPrintf() */
#define ERRMSG          0x01     /* Prefix the <ErrorString> to the front of the message */
#define TIME_STAMP      0x02     /* Display a timestamp on the line before the message */
#define LINEBEFORE      0x04     /* Display separating lines before and after the message */
#define LINEAFTER       0x08     /* Display a separating line after the message */
#define ENDLINE         LINEAFTER/* Some older programs used to use ENDLINE instead of LINEAFTER */
#define INTERNALERRMSG  0x10     /* Prefix ***INTERNAL ERROR: to the fromt of the message */
#define SHORTTIMESTAMP  0x20     /* Prefix a shorter timestamp to the front of the message */
#define INFO                    0x30     /* Will print only if debugMode = on */

#define LogMsg(Options,format,...) do{if(Options) printf("[%s:%d] ", __FILE__, __LINE__);printf(format,##__VA_ARGS__);}while(0)


typedef  struct tabTestInfo
{
    SQLHANDLE henv;
    SQLHDBC hdbc;
    SQLHANDLE hstmt;
    char dsn[128];
    char user[128];
    char pwd[50];
    char url[128];
}TestInfo;


void charToTChar(char *szChar, TCHAR *szTChar)
{
#ifdef UNICODE
    int i;

    for (i = 0; szChar[i]; i++)
    {
        szTChar[i] = szChar[i];
    }
    szTChar[i] = 0;
#else
    sprintf(szTChar, szChar);
#endif
}
void tcharToChar(TCHAR *szTChar, char *szChar)
{
#ifdef UNICODE
    int i;

    for (i = 0; szTChar[i]; i++)
    {
        szChar[i] = szTChar[i] & 0x00ff;
    }

    szChar[i] = 0;
#else
    sprintf(szChar, szTChar);
#endif
}

int sqlErrors(SQLHANDLE henv, SQLHDBC hdbc, SQLHANDLE hstmt)
{
    TCHAR szError[501];
    TCHAR szSqlState[10];
    SQLINTEGER  nNativeError;
    SQLSMALLINT nErrorMsg;
    char szData[501] = {0};

    if ( hstmt ){
        while ( SQLError( henv, 
                        hdbc, 
                        hstmt, 
                        (SQLTCHAR *)szSqlState, 
                        &nNativeError, 
                        (SQLTCHAR *)szError, 
                        sizeof(szError) / sizeof(TCHAR),
                        &nErrorMsg ) == SQL_SUCCESS ){
            tcharToChar(szError, szData);
            LogMsg(NONE, "%s\n", szData);
            memset(szError, 0, sizeof(szError));
            memset(szSqlState, 0, sizeof(szSqlState));
        }
    }

    if ( hdbc ){
        while ( SQLError( henv, 
                            hdbc, 
                            0, 
                            (SQLTCHAR *)szSqlState, 
                            &nNativeError, 
                            (SQLTCHAR *)szError, 
                            sizeof(szError) / sizeof(TCHAR), 
                            &nErrorMsg ) == SQL_SUCCESS ){
            tcharToChar(szError, szData);
            LogMsg(NONE, "%s\n", szData);
            memset(szError, 0, sizeof(szError));
            memset(szSqlState, 0, sizeof(szSqlState));
        }
    }

    if ( henv ){
        while ( SQLError( henv, 
                            0, 
                            0, 
                            (SQLTCHAR *)szSqlState, 
                            &nNativeError, 
                            (SQLTCHAR *)szError, 
                            sizeof(szError) / sizeof(TCHAR),
                            &nErrorMsg ) == SQL_SUCCESS ){
            tcharToChar(szError, szData);
            LogMsg(NONE, "%s\n", szData);
            memset(szError, 0, sizeof(szError));
            memset(szSqlState, 0, sizeof(szSqlState));
        }
    }

    return 1;
}

int connectDb(TestInfo *pTestInfo, int Options)
{
    RETCODE returncode;
    SQLHANDLE henv = SQL_NULL_HANDLE;
    SQLHANDLE hdbc = SQL_NULL_HANDLE;
    TCHAR OutString[1024] = {0x0};
    SQLSMALLINT olen = 0;
    TCHAR connectStr[1024] = {0x0};
    char szDdl[1024] = {0};
    
	assert( (Options&CONNECT_ODBC_VERSION_2) || (Options&CONNECT_ODBC_VERSION_3) );

	/* Initialize the basic handles needed by ODBC */
	if (Options&CONNECT_ODBC_VERSION_2)
	{
		returncode = SQLAllocEnv(&henv);
		if ((returncode != SQL_SUCCESS) && (returncode != SQL_SUCCESS_WITH_INFO)){
            return -1;
        }
		
		returncode = SQLAllocConnect(henv,&hdbc);
		if ((returncode != SQL_SUCCESS) && (returncode != SQL_SUCCESS_WITH_INFO)){
			/* Cleanup.  No need to check return codes since we are already failing */
			SQLFreeEnv(henv);
			return -1;
		}
	}
	if (Options&CONNECT_ODBC_VERSION_3)
	{
		returncode = SQLAllocHandle(SQL_HANDLE_ENV, SQL_NULL_HANDLE, &henv);
		if ((returncode != SQL_SUCCESS) && (returncode != SQL_SUCCESS_WITH_INFO)){
            return -1;
        }

		returncode = SQLSetEnvAttr(henv, SQL_ATTR_ODBC_VERSION, (SQLPOINTER) SQL_OV_ODBC3, 0);
		if ((returncode != SQL_SUCCESS) && (returncode != SQL_SUCCESS_WITH_INFO)){
			/* Cleanup. No need to check return codes since we are already failing */
			SQLFreeHandle(SQL_HANDLE_ENV, henv);
			return -1;
		}

		returncode = SQLAllocHandle(SQL_HANDLE_DBC, henv, &hdbc);
		if ((returncode != SQL_SUCCESS) && (returncode != SQL_SUCCESS_WITH_INFO)){
			/* Cleanup. No need to check return codes since we are already failing */
			SQLFreeHandle(SQL_HANDLE_ENV, henv);
			return -1;
		}
	}

	// Handle Autocommit_Off Option
	if (Options&CONNECT_AUTOCOMMIT_OFF)
    {/*
		returncode = SQLSetConnectOption(hdbc, SQL_AUTOCOMMIT, SQL_AUTOCOMMIT_OFF);
		if ((returncode != SQL_SUCCESS) && (returncode != SQL_SUCCESS_WITH_INFO)){
			// Cleanup.  No need to check return codes since we are already failing
			SQLFreeEnv(henv);
			return -1;
		}*/
	}  
#if 0
    sprintf(szDdl, "DSN=%s;UID=%s;PWD=%s;%s", pTestInfo->DataSource, pTestInfo->UserID, pTestInfo->Password, connStrEx);
    charToTChar(szDdl, connectStr);
    returncode = SQLDriverConnect(hdbc, NULL, (SQLTCHAR*)connectStr, SQL_NTS, (SQLTCHAR*)OutString, sizeof(OutString), &olen, SQL_DRIVER_NOPROMPT);
#else
    TCHAR dsn[128], user[128], pwd[128];

    charToTChar(pTestInfo->dsn, dsn);
    charToTChar(pTestInfo->user, user);
    charToTChar(pTestInfo->pwd, pwd);
    returncode = SQLConnect(hdbc, (SQLTCHAR *)dsn, SQL_NTS, (SQLTCHAR *)user, SQL_NTS, (SQLTCHAR *)pwd, SQL_NTS);
#endif
    if ((returncode != SQL_SUCCESS) && (returncode != SQL_SUCCESS_WITH_INFO))
	{
	    LogMsg(NONE, "[%s:%d]Call SQLConnect fail.\n", __FILE__, __LINE__);
        sqlErrors(henv, hdbc, SQL_NULL_HANDLE);
		// Free up handles since we hit a problem.
		if (Options&CONNECT_ODBC_VERSION_2){
			/* Cleanup.  No need to check return codes since we are already failing */
			SQLFreeConnect(hdbc);
			SQLFreeEnv(henv);
			return -1;
		}
		if (Options&CONNECT_ODBC_VERSION_3){
			/* Cleanup.  No need to check return codes since we are already failing */		   
			SQLFreeHandle(SQL_HANDLE_DBC, hdbc);
			SQLFreeHandle(SQL_HANDLE_ENV, henv);
            return -1;
		}
        return -1;
	}

	pTestInfo->henv = (SQLHANDLE)henv;
	pTestInfo->hdbc = (SQLHANDLE)hdbc;
	
	/* Connection established */
	return 0;
}
int disconnectDb(SQLHANDLE henv, SQLHDBC hdbc, SQLHANDLE hstmt)
{
    RETCODE retcode;                        
    int ret = 0;

    if(hdbc){
        retcode = SQLDisconnect(hdbc);
        if ((retcode != SQL_SUCCESS) && (retcode != SQL_SUCCESS_WITH_INFO)){
            ret = -1;	
            sqlErrors(henv, hdbc, hstmt);
        }
        retcode = SQLFreeConnect(hdbc);
        if ((retcode != SQL_SUCCESS) && (retcode != SQL_SUCCESS_WITH_INFO)){
            ret = -1;	
            sqlErrors(henv, hdbc, hstmt);
        }
    }
    if(henv){
        retcode = SQLFreeEnv(henv);
        if ((retcode != SQL_SUCCESS) && (retcode != SQL_SUCCESS_WITH_INFO)){
            ret = -1;	
            sqlErrors(henv, hdbc, hstmt);
        }
    }

    return ret;
}

struct rowsetDataPara
{
    int tend_group;
    int mesg_seq;
    int conv_index;
    int conv_cindex;
    SQLLEN lenGroup;
    SQLLEN lenSeq;
    SQLLEN lenIndex;
    SQLLEN lenCindex;
};

int testDemo(TestInfo *pTestInfo)
{
    int ret = 0;
    SQLLEN len;
    RETCODE retcode;
    SQLHANDLE   henv = SQL_NULL_HANDLE;
    SQLHANDLE   hdbc = SQL_NULL_HANDLE;
    SQLHANDLE   hstmt = SQL_NULL_HANDLE;
    TestInfo mTestInfo;
    #define CONFIG_ROWSET_SIZE  2
    SQLUSMALLINT pParamStatus[CONFIG_ROWSET_SIZE];
    int num, i;
    int structSize = sizeof(struct rowsetDataPara);
    int batchSize = CONFIG_ROWSET_SIZE;
    struct rowsetDataPara mRowsetData[CONFIG_ROWSET_SIZE];
    SQLULEN iParamsProcessed = 0;
    
    memcpy(&mTestInfo, pTestInfo, sizeof(TestInfo));

    if(connectDb(&mTestInfo, CONNECT_ODBC_VERSION_3) != 0){
        LogMsg(INFO, "connect fail: line %d\n");
        sqlErrors(henv,hdbc,hstmt);
        return -1;
    }
    henv = mTestInfo.henv;
    hdbc = mTestInfo.hdbc;
    
    retcode = SQLAllocHandle(SQL_HANDLE_STMT, hdbc, &hstmt);
    if ((retcode != SQL_SUCCESS) && (retcode != SQL_SUCCESS_WITH_INFO)){
        LogMsg(INFO, "SQLAllocHandle(SQL_HANDLE_STMT, hdbc, &hstmt) fail!\n");
        LogMsg(INFO,"Try SQLAllocStmt((SQLHANDLE)hdbc, &hstmt)\n");
        retcode = SQLAllocStmt((SQLHANDLE)hdbc, &hstmt);
        if ((retcode != SQL_SUCCESS) && (retcode != SQL_SUCCESS_WITH_INFO)){
            LogMsg(INFO,"SQLAllocStmt hstmt fail!\n");
            disconnectDb(henv, hdbc, hstmt);
            return -1;
        }
    }
    /********************************************************************/
    //test
    char *szDdl = "CREATE TABLE T (TEND_GROUP int, MESG_SEQ int, CONV_INDEX int, CONV_CINDEX int)";
    char *szUpdate = "UPDATE T SET TEND_GROUP = ? WHERE MESG_SEQ = ? AND CONV_INDEX = ? AND CONV_CINDEX = ?";
    char *szTable = "T";
    char szBuf[512] = {0};
    int tend_group, mesg_seq, conv_index, conv_cindex;
    SWORD numOfCols, index;
    
    sprintf(szBuf, "DROP TABLE %s", szTable);
    printf("%s\n", szBuf);
    retcode = SQLExecDirect(hstmt, (SQLTCHAR*)szBuf, SQL_NTS);
    printf("%s\n", szDdl);
    retcode = SQLExecDirect(hstmt, (SQLTCHAR*)szDdl, SQL_NTS);
    if ((retcode != SQL_SUCCESS) && (retcode != SQL_SUCCESS_WITH_INFO)){
        printf("call SQLExecDirect fail:\n");
        sqlErrors(henv, hdbc, hstmt);
        disconnectDb(henv, hdbc, hstmt);
        return -1;
    }
    for(i = 0; i < 20; i++){
        sprintf(szBuf, "INSERT INTO %s values(%d, %d, %d, %d)", szTable, i, i, i, i);
        retcode = SQLExecDirect(hstmt, (SQLTCHAR*)szBuf, SQL_NTS);
        if ((retcode != SQL_SUCCESS) && (retcode != SQL_SUCCESS_WITH_INFO)){
            printf("call SQLExecDirect fail:\n");
            sqlErrors(henv, hdbc, hstmt);
            disconnectDb(henv, hdbc, hstmt);
            return -1;
        }
        if(i % 2 == 0){
            retcode = SQLExecDirect(hstmt, (SQLTCHAR*)szBuf, SQL_NTS);
            retcode = SQLExecDirect(hstmt, (SQLTCHAR*)szBuf, SQL_NTS);
            retcode = SQLExecDirect(hstmt, (SQLTCHAR*)szBuf, SQL_NTS);
        }
    }
    for(i = 100; i < 120; i++){
        sprintf(szBuf, "INSERT INTO %s values(%d, %d, %d, %d)", szTable, i, i, i, i);
        retcode = SQLExecDirect(hstmt, (SQLTCHAR*)szBuf, SQL_NTS);
        if ((retcode != SQL_SUCCESS) && (retcode != SQL_SUCCESS_WITH_INFO)){
            printf("call SQLExecDirect fail:\n");
            sqlErrors(henv, hdbc, hstmt);
            disconnectDb(henv, hdbc, hstmt);
            return -1;
        }
        if(i % 2 == 0){
            retcode = SQLExecDirect(hstmt, (SQLTCHAR*)szBuf, SQL_NTS);
            retcode = SQLExecDirect(hstmt, (SQLTCHAR*)szBuf, SQL_NTS);
            retcode = SQLExecDirect(hstmt, (SQLTCHAR*)szBuf, SQL_NTS);
        }
    }
    sprintf(szBuf, "select * from %s", szTable);
    retcode = SQLExecDirect(hstmt, (SQLTCHAR*)szBuf, SQL_NTS);
    if ((retcode != SQL_SUCCESS) && (retcode != SQL_SUCCESS_WITH_INFO)){
        printf("call SQLExecute fail:\n");
        sqlErrors(henv, hdbc, hstmt);
    }
    else{
        retcode = SQLFetch(hstmt);
        numOfCols = 0;
        retcode = SQLNumResultCols(hstmt, &numOfCols);
        printf("TEND_GROUP    MESG_SEQ     CONV_INDEX    CONV_CINDEX\n");
        while ((retcode != SQL_NO_DATA) && (numOfCols != 0)){
            for (index = 1; index <= numOfCols; index++){
                memset(szBuf, 0, sizeof(szBuf));
                retcode = SQLGetData(hstmt, index, SQL_C_CHAR, (SQLPOINTER)szBuf, sizeof(szBuf), &len);
                if (SQL_SUCCEEDED(retcode)){
                    printf("%s\t", szBuf);
                }
                else{
                    break;
                }
            }
            printf("\n");
            retcode = SQLFetch(hstmt);
        }
    }
    SQLFreeStmt(hstmt,SQL_CLOSE);
    printf("Init table success.\n");
    
    printf("%s\n", szUpdate);
    retcode = SQLPrepare(hstmt, (SQLTCHAR*)szUpdate, SQL_NTS);
    if ((retcode != SQL_SUCCESS) && (retcode != SQL_SUCCESS_WITH_INFO)){
        printf("call SQLPrepare fail:\n");
        sqlErrors(henv, hdbc, hstmt);
        disconnectDb(henv, hdbc, hstmt);
        return -1;
    }
    retcode = SQLSetStmtAttr(hstmt, SQL_ATTR_PARAM_BIND_TYPE, (SQLPOINTER)sizeof(struct rowsetDataPara), 0);
    retcode = SQLSetStmtAttr(hstmt, SQL_ATTR_PARAMSET_SIZE, (SQLPOINTER)CONFIG_ROWSET_SIZE, 0);
    retcode = SQLSetStmtAttr(hstmt, SQL_ATTR_PARAM_STATUS_PTR, pParamStatus, 0);
    retcode = SQLSetStmtAttr(hstmt, SQL_ATTR_PARAMS_PROCESSED_PTR, &iParamsProcessed, 0);
    
    mRowsetData[0].lenGroup = SQL_NTS;
    retcode = SQLBindParameter(hstmt, 
                                1, 
                                SQL_PARAM_INPUT , 
                                SQL_C_ULONG, 
                                SQL_INTEGER, 
                                0, 
                                0, 
                                &mRowsetData[0].tend_group, 
                                0, 
                                &mRowsetData[0].lenGroup);
    mRowsetData[0].lenSeq = SQL_NTS;
    retcode = SQLBindParameter(hstmt, 
                                2, 
                                SQL_PARAM_INPUT , 
                                SQL_C_ULONG, 
                                SQL_INTEGER, 
                                0, 
                                0, 
                                &mRowsetData[0].mesg_seq, 
                                0, 
                                &len);
    mRowsetData[0].lenIndex = SQL_NTS;
    retcode = SQLBindParameter(hstmt, 
                                3, 
                                SQL_PARAM_INPUT , 
                                SQL_C_ULONG, 
                                SQL_INTEGER, 
                                0, 
                                0, 
                                &mRowsetData[0].conv_index, 
                                0, 
                                &mRowsetData[0].lenIndex);
    mRowsetData[0].lenCindex = SQL_NTS;
    retcode = SQLBindParameter(hstmt, 
                                4, 
                                SQL_PARAM_INPUT , 
                                SQL_C_ULONG, 
                                SQL_INTEGER, 
                                0, 
                                0, 
                                &mRowsetData[0].conv_index, 
                                0, 
                                &mRowsetData[0].lenCindex);
    if ((retcode != SQL_SUCCESS) && (retcode != SQL_SUCCESS_WITH_INFO)){
        printf("call SQLBindParameter fail.\n");
        sqlErrors(henv, hdbc, hstmt);
        disconnectDb(henv, hdbc, hstmt);
        return -1;
    }

    //批量更新两条
    
    mRowsetData[0].tend_group = 12345;
    mRowsetData[0].mesg_seq = 2;
    mRowsetData[0].conv_index = 2;
    mRowsetData[0].conv_cindex = 2;
    mRowsetData[0].lenGroup = SQL_NTS;
    mRowsetData[0].lenSeq = SQL_NTS;
    mRowsetData[0].lenIndex = SQL_NTS;
    mRowsetData[0].lenCindex = SQL_NTS;

    mRowsetData[1].tend_group = 2456;
    mRowsetData[1].mesg_seq = 100;
    mRowsetData[1].conv_index = 100;
    mRowsetData[1].conv_cindex = 100;
    mRowsetData[1].lenGroup = SQL_NTS;
    mRowsetData[1].lenSeq = SQL_NTS;
    mRowsetData[1].lenIndex = SQL_NTS;
    mRowsetData[1].lenCindex = SQL_NTS;

    printf("Set update data: TEND_GROUP = %d MESG_SEQ = %d CONV_INDEX = %d  CONV_CINDEX = %d\n",
            mRowsetData[0].tend_group, 
            mRowsetData[0].mesg_seq,
            mRowsetData[0].conv_index,
            mRowsetData[0].conv_cindex);
    printf("Set update data: TEND_GROUP = %d MESG_SEQ = %d CONV_INDEX = %d  CONV_CINDEX = %d\n",
            mRowsetData[1].tend_group, 
            mRowsetData[1].mesg_seq,
            mRowsetData[1].conv_index,
            mRowsetData[1].conv_cindex);
    printf("call SQLExecute\n");
    retcode = SQLExecute(hstmt);
    if ((retcode != SQL_SUCCESS) && (retcode != SQL_SUCCESS_WITH_INFO)){
        printf("call SQLExecute fail:\n");
        sqlErrors(henv, hdbc, hstmt);
        disconnectDb(henv, hdbc, hstmt);
        return -1;
    }

    SQLFreeStmt(hstmt,SQL_CLOSE);
    
    printf("checking.....\n");
    sprintf(szBuf, "select * from %s", szTable);
    printf("%s\n", szBuf);
    retcode = SQLExecDirect(hstmt, (SQLTCHAR*)szBuf, SQL_NTS);
    if ((retcode != SQL_SUCCESS) && (retcode != SQL_SUCCESS_WITH_INFO)){
        printf("call SQLExecute fail:\n");
        sqlErrors(henv, hdbc, hstmt);
    }
    else{
        retcode = SQLFetch(hstmt);
        numOfCols = 0;
        retcode = SQLNumResultCols(hstmt, &numOfCols);
        printf("TEND_GROUP    MESG_SEQ     CONV_INDEX    CONV_CINDEX\n");
        while ((retcode != SQL_NO_DATA) && (numOfCols != 0)){
            for (index = 1; index <= numOfCols; index++){
                memset(szBuf, 0, sizeof(szBuf));
                retcode = SQLGetData(hstmt, index, SQL_C_CHAR, (SQLPOINTER)szBuf, sizeof(szBuf), &len);
                if (SQL_SUCCEEDED(retcode)){
                    printf("%s\t", szBuf);
                }
                else{
                    break;
                }
            }
            printf("\n");
            retcode = SQLFetch(hstmt);
        }
    }
    /********************************************************************/
    disconnectDb(henv, hdbc, hstmt);
    LogMsg(TIME_STAMP, "complete test:%s\n", (ret == 0) ? "sucess" : "fail"); 
    
    return ret;
}
void help(void)
{
    printf("\t-h:print this help\n");
    printf("Connection related options. You can connect using either:\n");
    printf("\t-d Data_Source_Name\n");
    printf("\t-u user\n");
    printf("\t-p password\n");
    printf("\t-s schema name\t");
    printf("sample:\t./debug -d traf -u trafodion -p traf123\n");
}

//g++ test_odbc.c -o test -Dunixcli -g -w -lodbc
//./debug -d traf -u trafodion -p traf123
int main(int argc, char *argv[])
{
    char buf[256] = {0};
    int num;
    int ret;
    SQLLEN len;
    TestInfo mTestInfo;

    memset(&mTestInfo, 0, sizeof(TestInfo));

    if(argc <= 1){
        help();
        return -1;
    }
    for(num = 1; num < argc; num++){
        if(strcmp(argv[num], "-h") == 0){
            help();
            return 0;
        }
        else if((strcmp(argv[num], "-d") == 0) && (argc > (num + 1))){
            num++;
            strcpy (mTestInfo.dsn, argv[num]);
        }
        else if((strcmp(argv[num], "-u") == 0) && (argc > (num + 1))){
            num++;
            strcpy (mTestInfo.user, argv[num]);
        }
        else if((strcmp(argv[num], "-p") == 0) && (argc > (num + 1))){
            num++;
            strcpy (mTestInfo.pwd, argv[num]);
        }
    }
    if(strlen(mTestInfo.dsn) == 0 ||
        strlen(mTestInfo.user) == 0 ||
        strlen(mTestInfo.pwd) == 0
        ){
        help();
#ifndef unixcli
        printf("Please enter any key exit\n");
        getchar();
#endif
        return -1;
    }

    testDemo(&mTestInfo);
    
#ifndef unixcli
    printf("Please enter any key exit\n");
    getchar();
#endif
    return 0;
}

