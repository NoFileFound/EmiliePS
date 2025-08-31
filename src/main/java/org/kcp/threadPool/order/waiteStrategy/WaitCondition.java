package org.kcp.threadPool.order.waiteStrategy;

public interface WaitCondition<T> {
	T getAttach();
}