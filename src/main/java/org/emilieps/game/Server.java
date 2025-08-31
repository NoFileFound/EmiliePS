package org.emilieps.game;

// Imports
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.emilieps.game.connection.ClientHandler;
import org.emilieps.game.connection.ClientSession;
import org.emilieps.game.packets.PacketHandler;
import org.emilieps.game.packets.RecvPacket;
import org.kcp.ChannelConfig;
import org.kcp.KcpListener;
import org.kcp.KcpServer;
import org.kcp.Ukcp;

public final class Server extends KcpServer {
    @Getter private final PacketHandler packetHandler;
    private final ClientHandler clientHandler;
    private final Map<Long, ClientSession> clientSessions;

    /**
     * Creates a new instance of the server.
     */
    public Server() {
        this.clientHandler = new ClientHandler(this);
        this.packetHandler = new PacketHandler(RecvPacket.class);
        this.clientSessions = new HashMap<>();

        var channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 40, 2, true);
        channelConfig.setMtu(1400);
        channelConfig.setSndwnd(256);
        channelConfig.setRcvwnd(256);
        channelConfig.setTimeoutMillis(15000);
        channelConfig.setUseConvChannel(true);
        channelConfig.setAckNoDelay(false);

        this.init(new KcpListener() {
            @Override
            public void onConnected(Ukcp ukcp) {
                ClientSession session = new ClientSession(Server.this, ukcp);
                clientSessions.put(ukcp.getConv(), session);
                clientHandler.onConnected(session);
            }

            @Override
            public void handleReceive(ByteBuf byteBuf, Ukcp ukcp) {
                ClientSession session = clientSessions.get(ukcp.getConv());
                if (session != null) {
                    clientHandler.onMessageReceived(session, ByteBufUtil.getBytes(byteBuf));
                }
            }

            @Override
            public void handleException(Throwable ex, Ukcp ukcp) {
                ClientSession session = clientSessions.get(ukcp.getConv());
                if (session != null) {
                    clientHandler.exceptionCaught(ex);
                }
            }

            @Override
            public void handleClose(Ukcp ukcp) {
                ClientSession session = clientSessions.get(ukcp.getConv());
                if (session != null) {
                    clientHandler.onClosed(session);
                    clientSessions.remove(ukcp.getConv());
                }
            }
        }, channelConfig, new InetSocketAddress("127.0.0.1", 8882));
    }
}