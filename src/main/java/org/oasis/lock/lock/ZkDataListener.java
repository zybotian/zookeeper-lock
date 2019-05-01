package org.oasis.lock.lock;

import org.I0Itec.zkclient.IZkDataListener;

public class ZkDataListener implements IZkDataListener {

    @Override
    public void handleDataChange(String path, Object data) {
        System.out.println("节点数据变化了,路径: " + path + ", 数据值: " + data);
    }

    @Override
    public void handleDataDeleted(String path) {
        System.out.println("节点被删除了,路径: " + path);
    }
}
