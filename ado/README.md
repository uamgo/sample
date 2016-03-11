###ADO.NET Driver Sample
######create a connection 
```
EsgynDBConnection conn = new EsgynDBConnection(); //create a connection
conn.ConnectionString = "server=10.0.0.5:23400;user=zz;password=zz;schema=ado";
conn.Open();
```
######perform a simple query
```
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
######None query
```
cmd.CommandText = "create table t0 (c1 varchar(20), c2 nchar(20))";
cmd.ExecuteNonQuery();
```
######Batch insert
```
cmd.CommandText = "insert into t0 values(?,?)";
//Define required parameters
cmd.Parameters.Add(new EsgynDBParameter("c0", EsgynDBType.Varchar));
cmd.Parameters.Add(new EsgynDBParameter("c1", EsgynDBType.Varchar));
//Do prepare for insertion SQL. Tips: parameter definition should be done first, then do prepare. 
cmd.Prepare();
try{  
  //fill data and add into batch
  for (int i = 0; i < 10; i++)
  {
    cmd.Parameters[0].Value = "test col1";
    cmd.Parameters[1].Value = "test col2";
    cmd.AddBatch();
  }
  //Execute Batch
  cmd.ExecuteNonQuery();  
catch(EsgynDBException e)  
{  
  for(int i=0; i<e.Errors.Count; i++)  
  {  
   Console.WriteLine(e.Errors[i]+“， ”+e.Errors[i].RowId);  
  }  
}  
```
