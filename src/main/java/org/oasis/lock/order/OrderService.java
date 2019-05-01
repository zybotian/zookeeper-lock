package org.oasis.lock.order;

public class OrderService implements IOrderService {

    private OrderIdGenerator generator = new OrderIdGenerator();

    @Override
    public void createOrder() {
        String orderId = generator.getOrderId();
        System.out.println(Thread.currentThread().getName() + "========>" + orderId);
        //  其他业务逻辑
    }
}
