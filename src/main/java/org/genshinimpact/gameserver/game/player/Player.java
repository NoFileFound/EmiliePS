package org.genshinimpact.gameserver.game.player;

// Imports
import java.util.List;
import lombok.Getter;
import org.genshinimpact.database.collections.Account;
import org.genshinimpact.database.collections.Guest;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.connection.SessionState;
import org.genshinimpact.gameserver.game.world.Scene;
import org.genshinimpact.gameserver.game.world.World;
import org.genshinimpact.gameserver.packets.SendPacket;
import org.genshinimpact.gameserver.packets.send.player.SendPlayerDataNotify;
import org.genshinimpact.webserver.responses.combo.reddot.RedDotListResponse;

public class Player {
    @Getter private final PlayerIdentity playerIdentity;
    private final ClientSession session;
    private World world;

    /**
     * Creates a new instance of Player.
     * @param account The player's account.
     */
    public Player(Account account, ClientSession session) {
        this.playerIdentity = account;
        this.session = session;
    }

    /**
     * Creates a new instance of Player.
     * @param guest The player's account (Guest).
     */
    public Player(Guest guest, ClientSession session) {
        this.playerIdentity = guest;
        this.session = session;
    }

    /**
     * Handles when the player leaves from the game.
     */
    public void closeConnection() {
        ///  TODO: FINISH
        this.session.getTunnel().close();
    }

    /**
     * Handles when the player logins in the game.
     */
    public void sendLogin() {
        this.world = new World(this);

        ///  TODO: Check if player is first logged today.
        this.sendPacket(new SendPlayerDataNotify(this.playerIdentity.getUsername(), true));
        ///  TODO: FINISH

        ///this.scene.initSceneLoading();
        this.session.setState(SessionState.ACTIVE);
    }

    /**
     * Sends a packet in the player.
     * @param packet The packet to send.
     */
    public void sendPacket(SendPacket packet) {
        this.session.sendPacket(packet);
    }


    public void setWorldPause(boolean isPaused) {
        this.world.setPaused(isPaused);
    }

    public List<RedDotListResponse.RedDot> getRedDots() {
        return List.of();
    }


    public record PlayerKey(long id, PlayerType type) {

    }

    public boolean isMultiplayer() {
        return this.world != null && this.world.getPlayers().size() > 1;
    }

    public enum PlayerType {
        ACCOUNT,
        GUEST
    }
}