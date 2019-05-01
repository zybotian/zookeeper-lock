package org.oasis.lock.lock;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class ZookeeperDistributedLock3 implements ILock {

    private String lockDir;

    private CuratorFramework client;

    private InterProcessMutex interProcessMutex;

    public ZookeeperDistributedLock3(String lockDir) {
        this.lockDir = lockDir;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient("127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183", retryPolicy);
        client.start();

        // 正常情况是Spring bean容器destroy时，执行close
        // 这里未引入spring，因此设计为在JVM退出时关闭客户端
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                client.close();
            }
        }));
    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public void lock() {
        interProcessMutex = new InterProcessMutex(client, lockDir);
        try {
            interProcessMutex.acquire();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void unlock() {
        try {
            interProcessMutex.release();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
