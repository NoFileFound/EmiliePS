package org.kcp;

// Imports
import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import lombok.Getter;
import lombok.Setter;

public class User {
    private Channel channel;
    @Getter private InetSocketAddress remoteAddress;
    private InetSocketAddress localAddress;
    @Setter private Object cache;

    public <T>  T getCache() {
        return (T) cache;
    }

    public User(Channel channel, InetSocketAddress remoteAddress, InetSocketAddress localAddress) {
        this.channel = channel;
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
    }

    protected Channel getChannel() {
        return channel;
    }

    protected void setChannel(Channel channel) {
        this.channel = channel;
    }

    protected void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    protected InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    protected void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    @Override
    public String toString() {
        return "User{" + "remoteAddress=" + remoteAddress + ", localAddress=" + localAddress + '}';
    }
}