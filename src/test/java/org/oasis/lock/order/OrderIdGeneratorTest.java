package org.oasis.lock.order;

public class OrderIdGeneratorTest {
    public static void main(String[] args) {
        OrderIdGenerator orderIdGenerator = new OrderIdGenerator();
        for (int i = 0; i < 50; i++) {
            System.out.println(orderIdGenerator.getOrderId());
        }
    }
}
