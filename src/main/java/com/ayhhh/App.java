package com.ayhhh;

import java.lang.String;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, InterruptedException {
        //zk是有session概念的，没有连接池的概念
        //每个连接都有独立的session
        //该连接是异步的。
        //watch有两类：（1）new zk时传入的，是session级别的，跟path没关系，只跟连接状态有关（2）监控节点的watch
        //watch的注册只发生在读方法中
        final CountDownLatch latch = new CountDownLatch(1);
        final ZooKeeper zk = new ZooKeeper(
                "10.236.11.45:2181",
                3000,
                new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                Event.KeeperState state = watchedEvent.getState();
                Event.EventType type = watchedEvent.getType();
                System.out.println(watchedEvent.toString());
                switch (state) {
                    case Unknown:
                        break;
                    case Disconnected:
                        break;
                    case NoSyncConnected:
                        break;
                    case SyncConnected:
                        System.out.println("connected");
                        latch.countDown();
                        break;
                    case AuthFailed:
                        break;
                    case ConnectedReadOnly:
                        break;
                    case SaslAuthenticated:
                        break;
                    case Expired:
                        break;
                    case Closed:
                        break;
                }
                switch (type) {
                    case None:
                        break;
                    case NodeCreated:
                        System.out.println("node created");
                        break;
                    case NodeDeleted:
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
        });
        try {
            latch.await();

            zk.create("/tlbb", "123".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

            // 返回的byte数组是1M的数据，而Stat里面放的是元数据，比如zxid等
            final Stat stat = new Stat();
            byte[] data = zk.getData("/tlbb", new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    System.out.println(event.toString());

                    //watch只能监控一次，如果想继续监控，则再次get
                    try {
                        zk.getData("/tlbb",this, stat);
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, stat);
            System.out.println(new String(data));
            System.out.println("create zxid: "+ stat.getCzxid());

            Stat stat1 = zk.setData("/tlbb", "456".getBytes(), 0);
            System.out.println(stat1.getCzxid());

            System.out.println("=========async start======");
            zk.getData(
                    "/tlbb",
                    false,
                    new AsyncCallback.DataCallback() {
                @Override
                public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                    System.out.println(new String(data));
                }
            },"111");
            System.out.println("=========async end======");
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }

        Thread.sleep(222222222);

    }
}
