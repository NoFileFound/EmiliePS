package org.genshinimpact.gameserver.game;

// Imports
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.net.InetSocketAddress;
import lombok.Getter;
import org.genshinimpact.gameserver.connection.ClientHandler;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.connection.kcp.KcpSession;
import org.genshinimpact.gameserver.packets.BadPacketException;
import org.kcp.ChannelConfig;
import org.kcp.KcpServer;
import org.kcp.Ukcp;

public final class Server extends KcpServer {
    @Getter private final Object2ObjectMap<Long, Player> players;
    private final Object2ObjectMap<Ukcp, KcpSession> sessions;

    /**
     * Initializes the game server.
     */
    public Server() {
        this.sessions = new Object2ObjectOpenHashMap<>();
        this.players = new Object2ObjectOpenHashMap<>();

        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 20, 2, true);
        channelConfig.setMtu(1400);
        channelConfig.setSndwnd(256);
        channelConfig.setRcvwnd(256);
        channelConfig.setTimeoutMillis(30 * 1000);
        channelConfig.setUseConvChannel(true);
        channelConfig.setAckNoDelay(false);
        this.init(new ClientHandler(this), channelConfig, new InetSocketAddress("127.0.0.1", 8882));
    }

    /**
     * Adds a new session.
     * @param ukcp The session's UKCP.
     * @param session The session's instance.
     */
    public void addSession(Ukcp ukcp, ClientSession session) {
        this.sessions.put(ukcp, session);
    }

    /**
     * Removes a session.
     * @param ukcp The session's UKCP.
     */
    public void removeSession(Ukcp ukcp) {
        if(this.sessions.containsKey(ukcp)) {
            this.sessions.get(ukcp).onClose();
            this.sessions.remove(ukcp);
        }
    }

    /**
     * Handles when a client session receives a data.
     * @param ukcp The session's UKCP.
     * @param data The session's data.
     * @throws BadPacketException The packet is invalid.
     */
    public void sessionReceiveData(Ukcp ukcp, ByteBuf data) throws BadPacketException {
        if(this.sessions.containsKey(ukcp)) {
            this.sessions.get(ukcp).onReceive(data);
        }
    }

    /**
     * Shutdowns the server.
     */
    public void shutdownServer() {

    }
}