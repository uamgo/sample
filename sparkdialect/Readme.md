代码调用：   
```
import org.apache.spark.sql.jdbc.TrafT4Dialect
import org.apache.spark.sql.jdbc.JdbcDialects
val dialect = new TrafT4Dialect 
JdbcDialects.registerDialect(dialect)

```