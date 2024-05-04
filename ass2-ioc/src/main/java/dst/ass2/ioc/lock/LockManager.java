package dst.ass2.ioc.lock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockManager {

    // Singleton
    private static LockManager instance;
    // Locks map
    private final Map<String, Lock> locks = new HashMap<>();

    public synchronized static LockManager getInstance() {
        if (instance == null) {
            instance = new LockManager();
        }
        return instance;
    }

    public synchronized void lock(String lockName) {
        Lock lock = locks.get(lockName);
        if (lock == null) {
            lock = new ReentrantLock();
            locks.put(lockName, lock);
        }
        lock.lock();
    }

    public synchronized void unlock(String lockName) {
        Lock lock = locks.get(lockName);
        if (lock == null) {
            return;
        }
        lock.unlock();
    }
}
