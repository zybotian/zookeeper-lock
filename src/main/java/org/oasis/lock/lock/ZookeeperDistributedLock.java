package org.oasis.lock.lock;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.concurrent.CountDownLatch;

/**
 * 基于zookeeper临时节点构建的分布式锁
 * 成功创建节点的客户端认为获取到了锁，释放锁时删除节点，其他客户端监听到节点被删除的事件时再次尝试加锁
 * 由于节点删除事件会广播给所有监听锁目录节点的客户端，而其实只有一个客户端才能获取到锁，即所谓的"惊群效应"，
 * 惊群效应会影响zookeeper集群的性能
 */
public class ZookeeperDistributedLock implements ILock {

    private String lockPath;

    private ZkClient zkClient;

    public ZookeeperDistributedLock(String lockPath) {
        this.lockPath = lockPath;
        this.zkClient = new ZkClient("127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183");
        this.zkClient.setZkSerializer(new StringZkSerializer());
    }

    // 非阻塞，尝试获取锁
    @Override
    public boolean tryLock() {
        try {
            // 创建临时节点，创建成功则认为获取到了锁
            this.zkClient.createEphemeral(this.lockPath);
            return true;
        } catch (Exception ex) {
            // 创建遇到了异常，认为获取锁失败
            return false;
        }
    }

    // 加锁，尝试加锁失败则等待
    @Override
    public void lock() {
        if (!tryLock()) {
            // 等待
            waitForLock();
            // 再次尝试加锁
            lock();
        }
    }


    private void waitForLock() {
        final CountDownLatch latch = new CountDownLatch(1);

        // 数据变化监听器
        IZkDataListener zkDataListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String path, Object data) {

            }

            @Override
            public void handleDataDeleted(String path) {
                // 监听到指定路径节点被删除的事件时，latch减为0
                System.out.println("监听到节点 " + path + " 被删除");
                latch.countDown();
            }
        };

        // 注册监听器，监听指定路径的节点变化事件
        this.zkClient.subscribeDataChanges(this.lockPath, zkDataListener);

        // 等待latch减为0，latch减为0说明节点被删除了，即有客户端释放了锁
        if (this.zkClient.exists(this.lockPath)) {
            try {
                latch.await();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        // 取消监听
        this.zkClient.unsubscribeDataChanges(this.lockPath, zkDataListener);
    }

    @Override
    public void unlock() {
        this.zkClient.delete(this.lockPath);
    }
}
