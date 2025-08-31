package org.kcp.threadPool.order.waiteStrategy;

// Imports
import java.util.concurrent.TimeUnit;

public interface WaitConditionStrategy
{
    <T> T waitFor(WaitCondition<T> waitCondition,long timeOut,TimeUnit unit) throws InterruptedException;

    void signalAllWhenBlocking();
}