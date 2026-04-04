package org.genshinimpact.gameserver.game.player;

// Imports
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.database.collections.Account;
import org.genshinimpact.database.collections.Guest;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.connection.SessionState;
import org.genshinimpact.gameserver.enums.ClimateType;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.account.AccountBase;
import org.genshinimpact.gameserver.game.storages.AvatarStorage;
import org.genshinimpact.gameserver.game.world.Scene;
import org.genshinimpact.gameserver.game.world.SceneLoadState;
import org.genshinimpact.gameserver.game.world.World;
import org.genshinimpact.gameserver.packets.SendPacket;
import org.genshinimpact.gameserver.packets.send.player.SendOpenStateUpdateNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendScenePlayerLocationNotify;
import org.genshinimpact.gameserver.packets.send.world.SendWorldPlayerLocationNotify;
import org.genshinimpact.webserver.responses.combo.reddot.RedDotListResponse;

public final class Player {
    @Getter private final AccountBase account;
    private final ClientSession session;
    @Getter private final Server server;
    @Getter private final String ipAddress;
    @Getter private final AvatarStorage avatarStorage;
    @Getter private long playerGameTime = 540000; // hardcoded in the game.
    @Getter private boolean isPaused;
    @Getter @Setter private World world;
    @Getter @Setter private int peerId;
    @Getter @Setter private Scene scene;
    @Getter @Setter private int sceneId;
    @Getter @Setter private int sceneEnterToken;
    @Getter @Setter private SceneLoadState sceneLoadState;
    @Getter private int weatherId = 0;
    @Getter private ClimateType climate = ClimateType.SUNNY;
    private int currentGuid = 0;

    /**
     * Creates a new instance of Player.
     * @param account The player's account.
     * @param session The player's session.
     */
    public Player(Account account, ClientSession session) {
        this.account = account;
        this.session = session;
        this.server = session.getServer();
        this.ipAddress = session.getTunnel().getAddress().getAddress().getHostAddress();
        this.avatarStorage = new AvatarStorage(this);
        this.world = new World(this);
    }

    /**
     * Creates a new instance of Player.
     * @param account The player's account (As a guest).
     * @param session The player's session.
     */
    public Player(Guest account, ClientSession session) {
        this.account = account;
        this.session = session;
        this.server = session.getServer();
        this.ipAddress = session.getTunnel().getAddress().getAddress().getHostAddress();
        this.avatarStorage = new AvatarStorage(this);
        this.world = new World(this);
    }

    /**
     * Checks if the player has logged today.
     * @return True if it has logged for first time today or else False.
     */
    public boolean getFirstLoginToday() {
        var ts = System.currentTimeMillis() / 1000;
        var isLoggedFirstTime = (this.account.getLastLoginDate() / 86400) < (ts / 86400);
        this.account.setLastLoginDate(ts);
        return isLoggedFirstTime;
    }

    /**
     * Gets the next global unique identifier.
     * @return The next global unique identifier.
     */
    public long getNextGuid() {
        long nextId = ++this.currentGuid;
        return(this.account.getId() << 32) + nextId;
    }

    /**
     * Gets the player's session state type.
     * @return The player's session state.
     */
    public SessionState getSessionState() {
        return this.session.getState();
    }

    /**
     * Sets the player's session state type.
     * @param state The session state to set on the current player.
     */
    public void setSessionState(SessionState state) {
        this.session.setState(state);
    }

    /**
     * Sends a packet to the player.
     * @param packet The packet to send.
     */
    public void sendPacket(SendPacket packet) {
        this.session.sendPacket(packet);
    }

    /**
     * Sends the login packets.
     */
    public void sendLogin() {
        this.setSessionState(SessionState.ACTIVE);
        this.world.addPlayer(this);
        this.sendPacket(new SendOpenStateUpdateNotify());

        this.server.getPlayers().put(this.account.getId(), this);
    }

    /**
     * Closes the player's connection.
     */
    public void closeConnection() {
        this.world.removePlayer(this);
        this.server.getPlayers().remove(this.account.getId());
        this.session.getTunnel().close();
        ///  TODO: FINISH
    }



    public List<RedDotListResponse.RedDot> getRedDots() {
        return List.of();
        ///  TODO: FINISH
    }

    public void sendUpdateLocation() {
        this.sendPacket(new SendWorldPlayerLocationNotify(this.world));
        this.sendPacket(new SendScenePlayerLocationNotify(this.scene));

    }

    public long getClientTime() {
        return this.session.getClientTime();
    }
}