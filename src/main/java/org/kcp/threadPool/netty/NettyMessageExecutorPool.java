package org.kcp.threadPool.netty;

// Imports
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.kcp.threadPool.IMessageExecutor;
import org.kcp.threadPool.IMessageExecutorPool;

public class NettyMessageExecutorPool implements IMessageExecutorPool {
    private final EventLoopGroup eventExecutors;
    protected static final AtomicInteger index = new AtomicInteger();

    public NettyMessageExecutorPool(int workSize){
        eventExecutors = new DefaultEventLoopGroup(workSize, new DefaultThreadFactory("nettyMessageExecutorPool"));
    }

    @Override
    public IMessageExecutor getIMessageExecutor() {
        return new NettyMessageExecutor(eventExecutors.next());
    }

    @Override
    public void stop() {
        if(!eventExecutors.isShuttingDown()) {
            eventExecutors.shutdownGracefully();
        }
    }
}