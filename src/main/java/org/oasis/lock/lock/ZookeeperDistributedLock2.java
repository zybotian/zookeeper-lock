package org.oasis.lock.lock;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 基于zookeeper的临时顺序节点实现的分布式锁
 * 加锁的操作就是z's's'z
 */
public class ZookeeperDistributedLock2 implements ILock {

    // zk client
    private ZkClient zkClient;

    // 锁的目录/service/lock
    private String lockDir;

    // 当前节点的路径 /service/lock/
    private String currentPath;

    // 当前节点的前一个节点的路径 /service/lock
    private String beforePath;

    public ZookeeperDistributedLock2(String lockDir) {
        this.lockDir = lockDir;
        this.zkClient = new ZkClient("127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183");
        this.zkClient.setZkSerializer(new StringZkSerializer());

        // 创建锁节点的根目录
        if (!this.zkClient.exists(this.lockDir)) {
            try {
                System.out.println(Thread.currentThread().getName() + "检查到锁目录不存在，于是去创建");
                this.zkClient.createPersistent(this.lockDir);
            } catch (Exception ex) {
                // ex.printStackTrace();
                System.out.println(Thread.currentThread().getName() + "检查到锁目录不存在，但是创建失败了");
            }
        }
    }

    // 非阻塞，尝试获取锁
    @Override
    public boolean tryLock() {
        if (this.currentPath == null || this.currentPath.isEmpty()) {
            // 第一次尝试加锁的时候，需要创建临时顺序节点
            this.currentPath = this.zkClient.createEphemeralSequential(this.lockDir + "/", "lock");
        }// 以后再尝试加锁时不需要再次创建临时顺序节点

        // 获取锁的目录下面子节点
        List<String> children = this.zkClient.getChildren(this.lockDir);

        // 对所有子节点进行排序
        // children为[0000000359,0000000360...]等10位数字
        // zookeeper底层将节点顺序号设计为int类型的整数，所以最大是int的最大值，达到最大会溢出变成一个绝对值很大的负数
        Collections.sort(children);

        // 如果当前节点就是最小的节点，则认为获取到了锁
        if (this.currentPath.equals(this.lockDir + "/" + children.get(0))) {
            return true;
        }

        // 当前节点不是最小的节点，则监听排在自己前面的那一个节点

        // 当前节点的index
        int currentIndex = Collections.binarySearch(children, this.currentPath.substring(this.lockDir.length() + 1));

        // 当前节点的前面一个节点
        this.beforePath = this.lockDir + "/" + children.get(currentIndex - 1);
        System.out.println("current path: " + this.currentPath + ", before path: " + this.beforePath);

        return false;
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
            public void handleDataChange(String s, Object o) {

            }

            @Override
            public void handleDataDeleted(String s) {
                // 监听到指定路径节点被删除的事件时，latch减为0
                System.out.println("节点被删除:" + s);
                latch.countDown();
            }
        };

        // 注册监听器，监听排在自己前面的的一个节点的被删除事件
        this.zkClient.subscribeDataChanges(this.beforePath, zkDataListener);

        // 等待latch减为0，latch减为0说明排在自己前面的节点被删除了，即有客户端释放了锁
        if (this.zkClient.exists(this.beforePath)) {
            try {
                latch.await();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        // 取消监听
        this.zkClient.unsubscribeDataChanges(this.beforePath, zkDataListener);
    }

    @Override
    public void unlock() {
        this.zkClient.delete(this.currentPath);
    }
}
