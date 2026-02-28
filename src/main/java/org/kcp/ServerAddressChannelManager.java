package org.kcp;

// Imports
import io.netty.channel.socket.DatagramPacket;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerAddressChannelManager implements IChannelManager {
    private final Map<SocketAddress, Ukcp> ukcpMap = new ConcurrentHashMap<>();

    @Override
    public Ukcp get(DatagramPacket msg) {
        return ukcpMap.get(msg.sender());
    }

    @Override
    public void New(SocketAddress socketAddress, Ukcp ukcp,DatagramPacket msg) {
        ukcpMap.put(socketAddress, ukcp);
    }

    @Override
    public void del(Ukcp ukcp) {
        ukcpMap.remove(ukcp.user().getRemoteAddress());
    }

    @Override
    public Collection<Ukcp> getAll() {
        return this.ukcpMap.values();
    }
}