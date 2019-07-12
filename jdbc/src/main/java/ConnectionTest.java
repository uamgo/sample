import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionTest {
  public static void main(String[] args) throws SQLException, ClassNotFoundException {
    String url = "jdbc:t4jdbc://" + args[0] + ":23400/:";
    String user = args[1];
    String pwd = args[2];
    String driverClass = "org.trafodion.jdbc.t4.T4Driver";
    Class.forName(driverClass);
    Connection connection = DriverManager.getConnection(url, user, pwd);
    ResultSet rs = connection.createStatement().executeQuery("select count(1) from dual");
    if(rs.next()){
      System.out.println("success---"+rs.getObject(1));
    }else{
      System.out.println();
    }
    connection.close();
  }
}
