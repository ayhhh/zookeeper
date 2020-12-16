package com.ayhhh.configurationcenter;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class WatchCallback implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {
    private ZooKeeper zooKeeper;
    private Configuration configuration;

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    private CountDownLatch latch = new CountDownLatch(1);

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setZooKeeper(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        //DataCallBack
        if(data != null && data.length != 0){
            String s = new String(data);
            configuration.setConfig(s);
            latch.countDown();
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        //StatCallback
        if(stat != null){ //stat == null代表节点不存在
            zooKeeper.getData("/appConfig",this,this,"miao~");
        }
    }

    @Override
    public void process(WatchedEvent event) {
        //Watcher
        System.out.println("my watch: "+ event.toString());

        //节点发生了事件之后的处理代码
        Event.EventType type = event.getType();
        switch (type) {
            case None:
                break;
            case NodeCreated:
                //所监听的节点才被注册，这次应当去拿数据
                //在这之前，WatchCallback.await()会一直等待
                zooKeeper.getData("/appConfig",this,this,"miao~");
                break;
            case NodeDeleted:
                //当节点被删除时，应该做什么？
                //（1）程序具有容忍性，删除了节点就使用以前的获取到的旧数据，此时不进行任何操作即可。
                //（2）删除节点，数据可能不一致了，没法玩。
                //这里实现第二种情况
                configuration.setConfig(null);

                break;
            case NodeDataChanged:
                //数据变更，则重新拿数据
                zooKeeper.getData("/appConfig",this,this,"miao~");
                break;
            case NodeChildrenChanged:
                break;
            case DataWatchRemoved:
                break;
            case ChildWatchRemoved:
                break;
        }
    }

    public void await() {
        zooKeeper.exists("/appConfig",this,this,"miao~");
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
