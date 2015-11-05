###ADO.NET Driver Sample
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

