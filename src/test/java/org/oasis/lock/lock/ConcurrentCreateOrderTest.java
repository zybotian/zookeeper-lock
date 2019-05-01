package org.oasis.lock.lock;

import org.oasis.lock.order.IOrderService;
import org.oasis.lock.order.OrderService;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

// 模拟单台服务器，不使用多线程同步机制
public class ConcurrentCreateOrderTest {

    public static void main(String[] args) {
        // 模拟并发数
        int currency = 50;

        // 循环栅栏
        final CyclicBarrier cb = new CyclicBarrier(currency);

        // 订单服务
        final IOrderService orderService = new OrderService();

        for (int i = 0; i < currency; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
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
