import org.apache.zookeeper.*;
import sun.applet.Main;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: bingshuai.lu
 * @Description:
 * @Date: Created in 14:44 2018/11/1
 * @Modified By:
 */
public class ZkUtil implements Watcher {
    private static final int SESSION_TIMEOUT=5000;
    private CountDownLatch latch = new CountDownLatch(1);
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getState() == Event.KeeperState.SyncConnected){
            latch.countDown();
        }
    }
    private ZooKeeper zooKeeper;
    public  void connect(String url) throws IOException, InterruptedException {
        zooKeeper =new ZooKeeper(url,SESSION_TIMEOUT,this);
        latch.await();
    }
    public void createGroup(String groupName) throws KeeperException, InterruptedException {
        String group = "/"+groupName;
        zooKeeper.create(group,null, ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);

    }
    public String join(String group, String memberName) throws KeeperException, InterruptedException {
        String path = "/"+group+"/"+memberName;
        String createPath = zooKeeper.create(path,null,ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
        return createPath;
    }
    public String getChild(String groupName) throws KeeperException, InterruptedException {
        String path = "/"+groupName;
        String res ="";
        List<String> childs = zooKeeper.getChildren(path,false);
        for (String str :
                childs) {
            res+=str;
        }
        return res;
    }
    public void deleteGroup(String groupName) throws KeeperException, InterruptedException {
        String path = "/"+groupName;
        try {
            List<String> childs = zooKeeper.getChildren(path,false);
            for (String str : childs){
                zooKeeper.delete(path+"/"+str,-1);
            }
            zooKeeper.delete(path,-1);
        }catch (KeeperException.NoNodeException e){
            throw e;
        }

    }
    public void close() throws InterruptedException {
        zooKeeper.close();
    }
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ZkUtil util = new ZkUtil();
        util.connect("127.0.0.1:2181");
        util.deleteGroup("testNode");
        //util.join("testNode","node1");
        String res = util.getChild("testNode");
        System.out.println(res);
        util.close();
    }

}
