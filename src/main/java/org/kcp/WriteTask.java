package org.kcp;

// Imports
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.Queue;
import org.kcp.erasure.fec.Snmp;
import org.kcp.threadPool.ITask;

public class WriteTask implements ITask {
    private final Ukcp ukcp;

    public WriteTask(Ukcp ukcp) {
        this.ukcp = ukcp;
    }

    @Override
    public void execute() {
        Ukcp ukcp = this.ukcp;
        try {
            if(!ukcp.isActive()){
                return;
            }

            Queue<ByteBuf> queue = ukcp.getWriteBuffer();
            int writeCount =0;
            long writeBytes = 0;
            while(ukcp.canSend(false)) {
                ByteBuf byteBuf = queue.poll();
                if(byteBuf == null){
                    break;
                }

                writeCount++;
                try {
                    writeBytes += byteBuf.readableBytes();
                    ukcp.send(byteBuf);
                    byteBuf.release();
                } catch(IOException e) {
                    ukcp.getKcpListener().handleException(e, ukcp);
                    return;
                }
            }

            Snmp.snmp.BytesSent.add(writeBytes);
            if(ukcp.isControlWriteBufferSize()){
                ukcp.getWriteBufferIncr().addAndGet(writeCount);
            }

            if(!ukcp.canSend(false) || (ukcp.checkFlush() && ukcp.isFastFlush())) {
                long now = System.currentTimeMillis();
                long next = ukcp.flush(now);
                ukcp.setTsUpdate(now+next);
            }
        } catch(Throwable e) {
            e.printStackTrace();
        } finally {
            release();
        }
    }

    public void release() {
        ukcp.getWriteProcessing().set(false);
    }
}