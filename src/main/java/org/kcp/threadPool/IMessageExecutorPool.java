package org.kcp.threadPool;

public interface IMessageExecutorPool {
    IMessageExecutor getIMessageExecutor();
    void stop();
}