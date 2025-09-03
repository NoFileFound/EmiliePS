package org.emilieps.game;

// Imports
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.net.InetSocketAddress;
import lombok.Getter;
import org.emilieps.Application;
import org.emilieps.database.Sanction;
import org.emilieps.game.connection.KcpSession;
import org.emilieps.game.packets.base.PacketHandler;
import org.emilieps.game.packets.PacketManager;
import org.emilieps.game.player.Player;
import org.emilieps.library.MongodbLib;
import org.kcp.ChannelConfig;
import org.kcp.KcpServer;
import org.kcp.Ukcp;

public final class GameServer extends KcpServer {
    @Getter private final PacketManager packetManager;
    @Getter private final Object2ObjectMap<Ukcp, KcpSession> sessions;
    @Getter private final Object2ObjectMap<Long, Player> players;
    @Getter private final Object2ObjectMap<Long, Sanction> cachedSanctions;

    /**
     * Initializes the game server.
     */
    public GameServer() {
        this.packetManager = new PacketManager(PacketHandler.class);
        this.sessions = new Object2ObjectOpenHashMap<>();
        this.players = new Object2ObjectOpenHashMap<>();
        this.cachedSanctions = new Object2ObjectOpenHashMap<>();


        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 20, 2, true);
        channelConfig.setMtu(1400);
        channelConfig.setSndwnd(256);
        channelConfig.setRcvwnd(256);
        channelConfig.setTimeoutMillis(30 * 1000);
        channelConfig.setUseConvChannel(true);
        channelConfig.setAckNoDelay(false);
        this.init(new GameServerListener(this), channelConfig, new InetSocketAddress(Application.getGameConfig().region.gateserver_ip, Application.getGameConfig().region.gateserver_port));
    }

    /**
     * Gets the latest sanction of the given player.
     *
     * @param accountId The account id.
     * @return A sanction object.
     */
    public Sanction getAccountLatestSanction(Long accountId) {
        if(this.cachedSanctions.get(accountId) != null) {
            Sanction mySanction = this.cachedSanctions.get(accountId);
            if(mySanction.getState().equals("Active")) {
                long time = mySanction.getExpirationDate();
                long currentTime = System.currentTimeMillis();
                if(mySanction.getIsPermanent() || time > currentTime) {
                    return mySanction;
                } else {
                    mySanction.setState("Expired");
                    mySanction.save();
                    return null;
                }
            }
        }

        Sanction mySanction = MongodbLib.findLatestSanction(accountId);
        if(mySanction != null) {
            System.out.println("OK");
            this.cachedSanctions.put(accountId, mySanction);
            return this.getAccountLatestSanction(accountId);
        }

        return null;
    }


    public Player getPlayerByAccountId(long accountId) {
        for(var player : this.players.values()) {
            if(player.getAccount().get_id() == accountId) return player;
        }

        return null;
    }
}