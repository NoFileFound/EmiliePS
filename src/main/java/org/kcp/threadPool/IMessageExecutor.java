package org.kcp.threadPool;

public interface IMessageExecutor {
	void stop();
	boolean isFull();
	void execute(ITask iTask);
}