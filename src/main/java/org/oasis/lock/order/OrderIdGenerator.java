package org.oasis.lock.order;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderIdGenerator {

    private int i = 0;

    public String getOrderId() {
        Date now = new Date();
        // 按照yyyy-MM-dd-HH-mm-ss-秒内序列号的格式创建订单号
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-");
        return sdf.format(now) + (++i);
    }
}
