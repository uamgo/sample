import java.io.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.conf.*;
 
public class WriteHdfsTest{
        public static void main (String [] args) throws Exception{
                try{
                        Path pt=new Path("hdfs://10.10.12.99:9000/tmp/test.txt");
                        FileSystem fs = FileSystem.get(new Configuration());
                        BufferedWriter br=new BufferedWriter(new OutputStreamWriter(fs.create(pt,true)));
                        String line;
                        line="Disha Dishu Daasha";
                        System.out.println(line);
                        br.write(line);
                        br.close();
                }catch(Exception e){
                        System.out.println("File not found");
                }
        }
}