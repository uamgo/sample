/*import java.io.IOException;
import java.util.Arrays;  
import java.util.List;  
  
import org.apache.commons.logging.Log;  
import org.apache.commons.logging.LogFactory;  
import org.apache.zookeeper.CreateMode;  
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;  
  
*//** 
 * TODO 
 * @author cuiran 
 * @version TODO 
 *//*  
public class ZooKeeperOperatorTest{
      
     public static void main(String[] args) {  
    	// 创建一个与服务器的连接
    	 ZooKeeper zk=null;
		try {
			zk = new ZooKeeper("localhost:2181", 
			        10000, new Watcher() { 
						@Override
						public void process(WatchedEvent event) {
							// TODO Auto-generated method stub
							
						} 
			        });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	 // 创建一个目录节点
    	 try {
			zk.create("/testRootPath", "testRootData".getBytes(), Ids.OPEN_ACL_UNSAFE,
			   CreateMode.PERSISTENT);
			// 创建一个子目录节点
	    	 zk.create("/testRootPath/testChildPathOne", "testChildDataOne".getBytes(),
	    	   Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT); 
	    	 System.out.println(new String(zk.getData("/testRootPath",false,null))); 
	    	 // 取出子目录节点列表
	    	 System.out.println(zk.getChildren("/testRootPath",true)); 
	    	 // 修改子目录节点数据
	    	 zk.setData("/testRootPath/testChildPathOne","modifyChildDataOne".getBytes(),-1); 
	    	 System.out.println("目录节点状态：["+zk.exists("/testRootPath",true)+"]"); 
	    	 // 创建另外一个子目录节点
	    	 zk.create("/testRootPath/testChildPathTwo", "testChildDataTwo".getBytes(), 
	    	   Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT); 
	    	 System.out.println(new String(zk.getData("/testRootPath/testChildPathTwo",true,null))); 
	    	 // 删除子目录节点
	    	 zk.delete("/testRootPath/testChildPathTwo",-1); 
	    	 zk.delete("/testRootPath/testChildPathOne",-1); 
	    	 // 删除父目录节点
	    	 zk.delete("/testRootPath",-1); 
	    	 // 关闭连接
	    	 zk.close();   
		} catch (KeeperException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	 
  
    }  
}  */