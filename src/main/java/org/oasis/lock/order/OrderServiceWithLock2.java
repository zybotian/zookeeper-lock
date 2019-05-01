package org.oasis.lock.order;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OrderServiceWithLock2 implements IOrderService {

    // 模拟order service部署在多个tomcat，但id generator只部署一台tomcat
    private static OrderIdGenerator generator = new OrderIdGenerator();

    private Lock lock = new ReentrantLock();

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
