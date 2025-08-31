package org.kcp.threadPool.order;

// Imports
import lombok.Getter;
import lombok.Setter;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jctools.queues.MpscLinkedQueue;

public class OrderedThreadSession {
    @Getter @Setter private int id;
    @Setter private Queue<Runnable> queue;
    private final AtomicBoolean processingCompleted = new AtomicBoolean(true);
    @Setter private int runTaskCount = 0;

    public OrderedThreadSession() {
        this(new MpscLinkedQueue<>());
    }

    public OrderedThreadSession(Queue<Runnable> queue) {
        this(queue, Integer.MAX_VALUE);
    }

    public OrderedThreadSession(Queue<Runnable> queue, int runTaskCount) {
        this.queue = queue;
        this.runTaskCount = runTaskCount;
    }

    public Queue<Runnable> getQueue() {
        return queue;
    }

    public AtomicBoolean getProcessingCompleted() {
        return processingCompleted;
    }

    public int getRunTaskCount() {
        return runTaskCount;
    }
}