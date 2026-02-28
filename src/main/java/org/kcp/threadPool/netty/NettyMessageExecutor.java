package org.kcp.threadPool.netty;

// Imports
import io.netty.channel.EventLoop;
import org.kcp.threadPool.IMessageExecutor;
import org.kcp.threadPool.ITask;

public class NettyMessageExecutor implements IMessageExecutor {
    private final EventLoop eventLoop;

    public NettyMessageExecutor(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public void execute(ITask iTask) {
        this.eventLoop.execute(iTask::execute);
    }
}