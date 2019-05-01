package org.oasis.lock.order;

import org.oasis.lock.lock.ILock;
import org.oasis.lock.lock.ZookeeperDistributedLock2;


public class OrderServiceWithDisLock2 implements IOrderService {

    // 模拟order service部署在多个tomcat，order id generator部署在单台tomcat
    private static OrderIdGenerator generator = new OrderIdGenerator();

    private ILock lock = new ZookeeperDistributedLock2("/order_service/lock2");

    @Override
    public void createOrder() {
        String orderId;
        try {
            lock.lock();
            orderId = generator.getOrderId();
        } finally {
            lock.unlock();
        }

        System.out.println(Thread.currentThread().getName() + "========>" + orderId);
        //  其他业务逻辑
    }
}
