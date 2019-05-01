package org.oasis.lock.lock;

import org.oasis.lock.order.IOrderService;
import org.oasis.lock.order.OrderServiceWithLock2;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

// 模拟多台tomcat服务器，但order code service服务部署成单独的一个服务，使用JDK锁
public class ConcurrentCreateOrderWithLock2Test {

    public static void main(String[] args) {
        // 模拟并发数
        int currency = 50;

        // 循环栅栏
        final CyclicBarrier cb = new CyclicBarrier(currency);

        for (int i = 0; i < currency; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 订单服务
                    IOrderService orderService = new OrderServiceWithLock2();
                    try {
                        // 等待所有线程都到达
                        cb.await();
                    } catch (InterruptedException | BrokenBarrierException ex) {
                        ex.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName() + "--------准备妥当-------");
                    // 调用订单服务
                    orderService.createOrder();
                }
            }).start();
        }
    }
}
