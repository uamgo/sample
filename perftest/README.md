查看使用手册：  
java -cp ~/tmp/jdbcT4-2.7.0.jar:target/perf-test-1.0-jar-with-dependencies.jar com.esgyn.perftest.EsgRunner -h  
```
usage: options  
-b,--batch <arg>     Batch size for testing. 100 by default.  
-e,--threads <arg>   The number of threads for testing. 5 by default.  
-h,--help            Usage:  
-i,--ip <arg>        Database IP address for testing.  
-o,--props <arg>     Database url properties for testing, for example:  
                      catalog=trafodion;schema=seabase. Use  
                      catalog=trafodion\;schema=seabase on terminal.  
-p,--port <arg>      Database port for testing, 23400 by default.  
-r,--rows <arg>      total rows for testing. 10000 by default.  
-t,--table <arg>     Table name with  for testing.  
-u,--user <arg>      Database user name for testing.  
-w,--pwd <arg>       Database password for testing.  
``` 
 
样例：  
java -cp ~/tmp/jdbcT4-2.7.0.jar:target/perf-test-1.0-jar-with-dependencies.jar com.esgyn.perftest.EsgRunner -i 10.10.10.14 -u trafodion -w traf123 -t SPN_MTWM_SMS_RECORD_48 -o schema=seabase
 
