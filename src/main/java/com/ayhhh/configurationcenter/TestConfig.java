package com.ayhhh.configurationcenter;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * 测试类
 * 对应客户端
 */
public class TestConfig {
    ZooKeeper zooKeeper;

    @Before
    public void connection(){
        zooKeeper = ZKUtils.getZooKeeper();
    }

    @After
    public void disconnection(){
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getConfiguration(){
        /*
        使用这种形式会非常复杂
        需要实现Watcher, StatCallback, DataCallback
        因此可以把三个接口封装一个自定义的类，达到复用
        zooKeeper.exists(
                "/appConfig",
                new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {

                    }
                },
                new AsyncCallback.StatCallback() {
                    @Override
                    public void processResult(int rc, String path, Object ctx, Stat stat) {

                    }
                },
                "miao~");
         */
        WatchCallback watchCallback = new WatchCallback();
        watchCallback.setZooKeeper(zooKeeper);
        Configuration configuration = new Configuration();
        watchCallback.setConfiguration(configuration);

        //下面这一步是异步的，在使用数据之前必须确定拿到了数据,因此需要阻塞住.把这部分代码封装到了WatchCallback.await()
        //zooKeeper.exists("/appConfig",watchCallback,watchCallback,"miao~");

        watchCallback.await(); //阻塞，知道第一次有数据

        //业务代码
        //上面的回调机制能够保证每时每刻configuration这个对象一直都是最新值
        while (true){
            if(configuration.getConfig() == null){
                System.out.println("configuration lost");

                watchCallback.setLatch(new CountDownLatch(1)); //重置CountDownLatch
                watchCallback.await(); //阻塞的等待数据
            } else {
                System.out.println(configuration.getConfig());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
