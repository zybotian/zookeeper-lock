package org.oasis.lock.lock;

public interface ILock {

    boolean tryLock();

    void lock();

    void unlock();

}
