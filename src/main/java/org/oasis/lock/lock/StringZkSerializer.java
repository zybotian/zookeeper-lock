package org.oasis.lock.lock;

import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;


public class StringZkSerializer implements ZkSerializer {

    public byte[] serialize(Object o) throws ZkMarshallingError {
        try {
            return o.toString().getBytes("UTF-8");
        } catch (Exception ex) {
            return new byte[0];
        }
    }

    public Object deserialize(byte[] bytes) throws ZkMarshallingError {
        try {
            return new String(bytes, "UTF-8");
        } catch (Exception ex) {
            return "";
        }

    }
}
