package org.oasis.lock.order;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OrderServiceWithLock implements IOrderService {

    // 模拟一个order service一个id generator的情况
    private OrderIdGenerator generator = new OrderIdGenerator();

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
