package com.ayhhh.configurationcenter;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZKUtils {
    private static ZooKeeper zooKeeper;
    private static DefaultWatch defaultWatcher = new DefaultWatch();

    //connection string, 这个string后面是可以接路径的，将该路径作为后续操作的根路径。这样每个服务使用自己的根路径
    private static String address = "10.236.11.45:2181";

    private static CountDownLatch latch = new CountDownLatch(1);

    public static ZooKeeper getZooKeeper() {
        try {
            defaultWatcher.setLatch(latch);
            zooKeeper = new ZooKeeper(
                    address,
                    3000,
                    defaultWatcher);

            //ZooKeeper的连接是异步的，因此必须等到完成连接之后才可以向下执行
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return zooKeeper;
    }
}
