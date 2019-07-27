// @@@ START COPYRIGHT @@@
//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//
// @@@ END COPYRIGHT @@@
package org.trafodion;

import java.sql.Types;

import org.hibernate.Hibernate;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.NvlFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;


public class TrafodionDialect extends Dialect {

  private static final int PARAM_LIST_SIZE_LIMIT = 1000;

  public TrafodionDialect() {
    super();
    registerCharacterTypeMappings();
    registerNumericTypeMappings();
    registerDateTimeTypeMappings();
    registerLargeObjectTypeMappings();
    registerReverseHibernateTypeMappings();
    registerFunctions();
    registerDefaultProperties();
  }

  protected void registerCharacterTypeMappings() {
    registerColumnType( Types.CHAR, "char(1)" );
    registerColumnType( Types.VARCHAR, "varchar($l)" );
  }

  protected void registerNumericTypeMappings() {
    registerColumnType( Types.BIT, "numeric(1,0)" );
    registerColumnType( Types.TINYINT, "numeric(1,0)" );
    registerColumnType( Types.SMALLINT, "smallint" );
    registerColumnType( Types.BIGINT, "largeint" );
    registerColumnType( Types.INTEGER, "integer" );

    registerColumnType( Types.FLOAT, "real" );
    registerColumnType( Types.DOUBLE, "double precision" );
    registerColumnType( Types.NUMERIC, "numeric($p,$s)" );
    registerColumnType( Types.DECIMAL, "decimal($p,$s)" );

    registerColumnType( Types.BOOLEAN, "numeric(1,0)" );
  }

  protected void registerDateTimeTypeMappings() {
    registerColumnType( Types.DATE, "date" );
    registerColumnType( Types.TIME, "time" );
    registerColumnType( Types.TIMESTAMP, "timestamp" );
  }

  protected void registerLargeObjectTypeMappings() {
    registerColumnType( Types.BINARY, "blob" );
    registerColumnType( Types.VARBINARY, "blob" );

    registerColumnType( Types.BLOB, "blob" );
    registerColumnType( Types.CLOB, "clob" );

    registerColumnType( Types.LONGVARCHAR, "clob" );
    registerColumnType( Types.LONGVARBINARY, "blob" );
  }

  protected void registerReverseHibernateTypeMappings() {
  }

  protected void registerFunctions() {
    registerFunction( "abs", new StandardSQLFunction("abs") );
    registerFunction( "sign", new StandardSQLFunction("sign", Hibernate.INTEGER) );

    registerFunction( "acos", new StandardSQLFunction("acos", Hibernate.DOUBLE) );
    registerFunction( "asin", new StandardSQLFunction("asin", Hibernate.DOUBLE) );
    registerFunction( "atan", new StandardSQLFunction("atan", Hibernate.DOUBLE) );
    registerFunction( "bitand", new StandardSQLFunction("bitand") );
    registerFunction( "cos", new StandardSQLFunction("cos", Hibernate.DOUBLE) );
    registerFunction( "cosh", new StandardSQLFunction("cosh", Hibernate.DOUBLE) );
    registerFunction( "exp", new StandardSQLFunction("exp", Hibernate.DOUBLE) );
    registerFunction( "log", new StandardSQLFunction("log", Hibernate.DOUBLE) );
    registerFunction( "sin", new StandardSQLFunction("sin", Hibernate.DOUBLE) );
    registerFunction( "sinh", new StandardSQLFunction("sinh", Hibernate.DOUBLE) );
    registerFunction( "stddev", new StandardSQLFunction("stddev", Hibernate.DOUBLE) );
    registerFunction( "sqrt", new StandardSQLFunction("sqrt", Hibernate.DOUBLE) );
    registerFunction( "tan", new StandardSQLFunction("tan", Hibernate.DOUBLE) );
    registerFunction( "tanh", new StandardSQLFunction("tanh", Hibernate.DOUBLE) );
    registerFunction( "variance", new StandardSQLFunction("variance", Hibernate.DOUBLE) );

    registerFunction( "round", new StandardSQLFunction("round") );
    registerFunction( "trunc", new StandardSQLFunction("trunc") );
    registerFunction( "ceiling", new StandardSQLFunction("ceiling") );
    registerFunction( "floor", new StandardSQLFunction("floor") );

    registerFunction( "char", new StandardSQLFunction("char", Hibernate.CHARACTER) );
    registerFunction( "lower", new StandardSQLFunction("lower") );
    registerFunction( "ltrim", new StandardSQLFunction("ltrim") );
    registerFunction( "rtrim", new StandardSQLFunction("rtrim") );
    registerFunction( "upper", new StandardSQLFunction("upper") );
    registerFunction( "ascii", new StandardSQLFunction("ascii", Hibernate.INTEGER) );

    registerFunction( "current_date", new NoArgSQLFunction("current_date", Hibernate.DATE, false) );
    registerFunction( "current_time", new NoArgSQLFunction("current_time", Hibernate.TIME, false) );
    registerFunction( "current_timestamp", new NoArgSQLFunction("current_timestamp", Hibernate.TIMESTAMP, false) );

    registerFunction( "user", new NoArgSQLFunction("user", Hibernate.STRING, false) );

    // Multi-param string dialect functions...
    registerFunction( "concat", new VarArgsSQLFunction(Hibernate.STRING, "", "||", "") );
    registerFunction( "instr", new StandardSQLFunction("instr", Hibernate.INTEGER) );
    registerFunction( "lpad", new StandardSQLFunction("lpad", Hibernate.STRING) );
    registerFunction( "replace", new StandardSQLFunction("replace", Hibernate.STRING) );
    registerFunction( "rpad", new StandardSQLFunction("rpad", Hibernate.STRING) );
    registerFunction( "substr", new StandardSQLFunction("substr", Hibernate.STRING) );
    registerFunction( "translate", new StandardSQLFunction("translate", Hibernate.STRING) );

    registerFunction( "substring", new StandardSQLFunction( "substr", Hibernate.STRING ) );
    registerFunction( "locate", new SQLFunctionTemplate( Hibernate.INTEGER, "instr(?2,?1)" ) );
    registerFunction( "coalesce", new NvlFunction() );

    // Multi-param numeric dialect functions...
    registerFunction( "atan2", new StandardSQLFunction("atan2", Hibernate.FLOAT) );
    registerFunction( "mod", new StandardSQLFunction("mod", Hibernate.INTEGER) );
    registerFunction( "nvl", new StandardSQLFunction("nvl") );
    registerFunction( "power", new StandardSQLFunction("power", Hibernate.FLOAT) );

    // Multi-param date dialect functions...
    registerFunction( "add_months", new StandardSQLFunction("add_months", Hibernate.DATE) );
  }

  protected void registerDefaultProperties() {
    getDefaultProperties().setProperty( Environment.USE_STREAMS_FOR_BINARY, "true" );
    getDefaultProperties().setProperty( Environment.STATEMENT_BATCH_SIZE, DEFAULT_BATCH_SIZE );
    getDefaultProperties().setProperty( Environment.USE_GET_GENERATED_KEYS, "true" );
  }
}