package org.genshinimpact.gameserver.game.player;

// Imports
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.database.collections.Account;
import org.genshinimpact.database.collections.Guest;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.connection.SessionState;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.world.Scene;
import org.genshinimpact.gameserver.game.world.World;
import org.genshinimpact.gameserver.packets.SendPacket;
import org.genshinimpact.webserver.responses.combo.reddot.RedDotListResponse;

// Packets
import org.genshinimpact.gameserver.packets.send.avatar.SendAvatarDataNotify;
import org.genshinimpact.gameserver.packets.send.inventory.SendStoreWeightLimitNotify;
import org.genshinimpact.gameserver.packets.send.player.SendPlayerDataNotify;

public class Player {
    @Getter private final PlayerIdentity playerIdentity;
    @Getter private final Server server;
    @Getter private final List<Long> tempAvatarGuidList;
    @Getter private final World world;
    @Getter private final Scene scene;
    @Getter @Setter private int peerId;
    private final ClientSession session;
    private int currentGuid = 0;
    @Getter private long playerGameTime = 540000;

    /**
     * Creates a new instance of Player.
     * @param account The player's account.
     */
    public Player(Account account, ClientSession session) {
        this.playerIdentity = account;
        this.session = session;
        this.server = session.getServer();
        this.world = new World(this);
        this.scene = new Scene(this);
        this.tempAvatarGuidList = new ArrayList<>();
    }

    /**
     * Creates a new instance of Player.
     * @param guest The player's account (Guest).
     */
    public Player(Guest guest, ClientSession session) {
        this.playerIdentity = guest;
        this.session = session;
        this.server = session.getServer();
        this.world = new World(this);
        this.scene = new Scene(this);
        this.tempAvatarGuidList = new ArrayList<>();
    }

    /**
     * Handles when the player leaves from the game.
     */
    public void closeConnection() {



        ///  TODO: FINISH
        this.session.getTunnel().close();
    }

    /**
     * Checks if the player has logged today.
     * @return True if it has logged for first time today or else False.
     */
    public boolean getIsFirstLoginToday() {
        var ts = System.currentTimeMillis() / 1000;
        var isLoggedFirstTime = (this.playerIdentity.getLastLoginDate() / 86400) < (ts / 86400);
        if(isLoggedFirstTime) {
            this.playerIdentity.setLastLoginDate(ts);
            return true;
        }

        return false;
    }

    /**
     * Gets the next global unique identifier.
     * @return The next global unique identifier.
     */
    public long getNextGuid() {
        long nextId = ++this.currentGuid;
        return (this.getPlayerIdentity().getId() << 32) + nextId;
    }

    /**
     * Handles when the player logins in the game.
     */
    public void sendLogin() {
        this.sendPacket(new SendPlayerDataNotify(this.playerIdentity.getUsername(), this.getIsFirstLoginToday()));
        this.sendPacket(new SendStoreWeightLimitNotify());
        for(var avatar : this.playerIdentity.getAvatars().values()) {
            avatar.loadAvatar(this);
        }

        this.sendPacket(new SendAvatarDataNotify(this));

        ///  TODO: FINISH

        this.scene.initSceneLoading();
        this.session.setState(SessionState.ACTIVE);
        ///this.server.setPlayer(this);
    }

    /**
     * Sends a packet in the player.
     * @param packet The packet to send.
     */
    public void sendPacket(SendPacket packet) {
        this.session.sendPacket(packet);
    }

    public boolean isFirstLoginEnterScene() {
        return this.session.getState() != SessionState.ACTIVE;
    }


    public List<RedDotListResponse.RedDot> getRedDots() {
        return List.of();
    }
}