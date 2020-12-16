package com.ayhhh.lock;

import com.ayhhh.configurationcenter.ZKUtils;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

public class TestLock {

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
    public void testDistributionLock(){

        //开启10个线程，模拟10个客户端抢锁
        for (int i = 0; i < 10; i++) {
            new Thread(()->{
                WatchCallBack watchCallBack = new WatchCallBack();
                watchCallBack.setZooKeeper(zooKeeper);
                String name = Thread.currentThread().getName();
                watchCallBack.setThreadName(name);

                //抢锁
                watchCallBack.tryLock();

                //do something
                System.out.println(name + " 运行... ");
                /*try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                //unlock
                watchCallBack.unLock();

            },"Thread "+i).start();
        }


        try {
            TimeUnit.SECONDS.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
