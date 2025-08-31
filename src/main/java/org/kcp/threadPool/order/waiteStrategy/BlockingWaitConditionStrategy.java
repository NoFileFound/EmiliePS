package org.kcp.threadPool.order.waiteStrategy;

// Imports
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class BlockingWaitConditionStrategy implements WaitConditionStrategy {
    private final Lock lock;
    private final Condition processorNotifyCondition;

    public BlockingWaitConditionStrategy() {
        this(false);
    }

    public BlockingWaitConditionStrategy(boolean fairSync) {
        this.lock = new ReentrantLock(fairSync);
        this.processorNotifyCondition = this.lock.newCondition();
    }

    public void signalAllWhenBlocking() {
        this.lock.lock();
        try {
            this.processorNotifyCondition.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public <T> T waitFor(WaitCondition<T> waitCondition, long timeOut, TimeUnit unit) throws InterruptedException {
        this.lock.lock();
        try {
            long waitTime = unit.toNanos(timeOut);
            T task = waitCondition.getAttach();
            if(task == null)
            {
                this.processorNotifyCondition.awaitNanos(waitTime);
                task = waitCondition.getAttach();
            }
            return task;
        } finally {
            this.lock.unlock();
        }
    }
}