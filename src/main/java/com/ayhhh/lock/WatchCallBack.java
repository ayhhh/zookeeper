package com.ayhhh.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WatchCallBack implements Watcher, AsyncCallback.StringCallback, AsyncCallback.Children2Callback, AsyncCallback.StatCallback {
    private ZooKeeper zooKeeper;
    private String threadName;
    private CountDownLatch latch = new CountDownLatch(1);
    private String pathName;

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    public void setZooKeeper(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public void tryLock(){
        zooKeeper.create("/distributionLock/lock",threadName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL,this,"miao~");

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void unLock(){
        try {
            //清除标记
            zooKeeper.setData("/distributionLock"," ".getBytes(),-1);
            zooKeeper.delete("/distributionLock/"+this.pathName,0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent event) {
        Event.EventType type = event.getType();
        switch (type) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                //前面的人已经删除了，有可能是中间不排队，直接离开了。
                //所以再次判断自己是不是第一个
                zooKeeper.getChildren("/distributionLock",false,this,"miao~");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
            case DataWatchRemoved:
                break;
            case ChildWatchRemoved:
                break;
        }

    }


    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        if(name != null){
            String[] split = name.split("/");
            setPathName(split[split.length-1]);
            System.out.println(threadName + " create " + name);
            //排队，检查自己的name是否是最小的
            zooKeeper.getChildren("/distributionLock",false,this,"miao~");
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        //当进入这个回调方法时，一定是自己的节点以及比自己早的节点已经创建完成了
        if(stat != null && children != null && children.size() != 0){
            Collections.sort(children);
            int i = children.indexOf(pathName);
            if(i == 0){
                try {
                    //这一步的目的是在父节点上面贴标签，以表明当前是谁在用锁。实现分布式重入锁 todo
                    zooKeeper.setData("/distributionLock",threadName.getBytes(),-1);
                    latch.countDown();
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else if(i<0){
                System.out.println("index out of boundary");
            } else {
                //不是第一个，则监控前面一个.
                //监控可能失败，有可能前面一个已经关闭了消失了
                zooKeeper.exists("/distributionLock/"+children.get(i-1),this,this,"miao~");

            }
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        //监控的节点不存在
        if(stat == null){
            //重新执行
            zooKeeper.getChildren("/distributionLock",false,this,"miao~");
        }
    }
}
