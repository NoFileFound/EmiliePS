package org.kcp;

// Imports
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueDatagramChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.HashedWheelTimer;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.kcp.erasure.fec.Fec;
import org.kcp.threadPool.IMessageExecutorPool;

public class KcpServer {
    private IMessageExecutorPool iMessageExecutorPool;
    private Bootstrap bootstrap;
    private EventLoopGroup group;
    private List<Channel> localAddresss = new Vector<>();
    @Getter private IChannelManager channelManager;
    private HashedWheelTimer hashedWheelTimer;

    private static class TimerThreadFactory implements ThreadFactory
    {
        private AtomicInteger timeThreadName = new AtomicInteger(0);
        @Override
        public Thread newThread(@NotNull Runnable r) {
            return new Thread(r,"KcpServerTimerThread " +timeThreadName.addAndGet(1));
        }
    }

    public void init(KcpListener kcpListener, ChannelConfig channelConfig, InetSocketAddress... addresses) {
        if(channelConfig.isUseConvChannel()) {
            int convIndex = 0;
            if(channelConfig.getFecAdapt() != null) {
                convIndex+= Fec.fecHeaderSizePlus2;
            }

            channelManager = new ServerConvChannelManager(convIndex);
        } else {
            channelManager = new ServerAddressChannelManager();
        }

        hashedWheelTimer = new HashedWheelTimer(new TimerThreadFactory(),1, TimeUnit.MILLISECONDS);
        boolean epoll = Epoll.isAvailable();
        boolean kqueue = KQueue.isAvailable();
        this.iMessageExecutorPool = channelConfig.getiMessageExecutorPool();
        bootstrap = new Bootstrap();
        int cpuNum = Runtime.getRuntime().availableProcessors();
        int bindTimes = 1;
        if(epoll||kqueue) {
            bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
            bindTimes = cpuNum;
        }

        Class<? extends Channel> channelClass = null;
        if(epoll) {
            group = new EpollEventLoopGroup(cpuNum);
            channelClass = EpollDatagramChannel.class;
        }else if(kqueue) {
            group = new KQueueEventLoopGroup(cpuNum);
            channelClass = KQueueDatagramChannel.class;
        }else {
            group = new NioEventLoopGroup(addresses.length);
            channelClass = NioDatagramChannel.class;
        }

        bootstrap.channel(channelClass);
        bootstrap.group(group);
        bootstrap.handler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {
                ServerChannelHandler serverChannelHandler = new ServerChannelHandler(channelManager, channelConfig, iMessageExecutorPool, kcpListener, hashedWheelTimer);
                ChannelPipeline cp = ch.pipeline();
                if(channelConfig.isCrc32Check()) {
                    Crc32Encode crc32Encode = new Crc32Encode();
                    Crc32Decode crc32Decode = new Crc32Decode();
                    cp.addLast(crc32Encode);
                    cp.addLast(crc32Decode);
                }

                cp.addLast(serverChannelHandler);
            }
        });

        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        for(InetSocketAddress address : addresses) {
            for(int i = 0; i < bindTimes; i++) {
                ChannelFuture channelFuture = bootstrap.bind(address);
                Channel channel = channelFuture.channel();
                localAddresss.add(channel);
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void stop() {
        localAddresss.forEach(ChannelOutboundInvoker::close);
        channelManager.getAll().forEach(Ukcp::close);
        if(iMessageExecutorPool != null) {
            iMessageExecutorPool.stop();
        }

        if(hashedWheelTimer != null){
            hashedWheelTimer.stop();
        }

        if(group != null) {
            group.shutdownGracefully();
        }
    }
}