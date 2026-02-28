package org.kcp.threadPool.order;

public interface IOrderTask extends Runnable{
    OrderedThreadSession getSession();
}