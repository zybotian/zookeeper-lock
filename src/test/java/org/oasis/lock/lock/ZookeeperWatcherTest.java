package org.oasis.lock.lock;

import org.I0Itec.zkclient.ZkClient;

public class ZookeeperWatcherTest {

    public static void main(String[] args) {
        // 创建连接到zk cluster的zk client
        ZkClient zkClient = new ZkClient("127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183");

        // 设置序列化反序列化
        zkClient.setZkSerializer(new StringZkSerializer());

        // 监控/paul/test节点的数据变化
        zkClient.subscribeDataChanges("/paul/test", new ZkDataListener());

        try {
            // main线程sleep，让zk client监听数据节点的变化
            Thread.sleep(10 * 60 * 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}