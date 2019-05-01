### 基于Zookeeper实现分布式锁的两种方式

#### 1. 基于zookeeper的临时节点

##### 1.1 实现流程图

![](https://github.com/zybotian/zookeeper-lock/blob/master/imgs/zookeeper_lock_1.png)

##### 1.2 实现代码

```text
    // 加锁：尝试加锁，失败时等待求他节点释放锁时再次抢锁
    @Override
    public void lock() {
        if (!tryLock()) {
            // 等待
            waitForLock();
            // 再次尝试加锁
            lock();
        }
    }
        
    // 尝试获取锁
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
        
    // 解锁： 删除临时节点
    @Override
    public void unlock() {
        this.zkClient.delete(this.lockPath);
    }
```

#### 2. 基于zookeeper的临时顺序节点

##### 2.1 实现流程图

![](https://github.com/zybotian/zookeeper-lock/blob/master/imgs/zookeeper_lock_2.png)

##### 2.2 实现代码

```text
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
```