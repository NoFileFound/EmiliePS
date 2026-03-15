package org.genshinimpact.gameserver.game.world;

// Imports
import lombok.Getter;
import org.genshinimpact.gameserver.game.player.Player;

// Packets
import org.genshinimpact.gameserver.packets.send.scene.SendPlayerEnterSceneNotify;

public class Scene {
    @Getter private int sceneId;
    @Getter private boolean isPaused;
    @Getter private int enterSceneToken;
    private final Player player;
    private final World world;
    private long startWorldTime;

    public Scene(Player player) {
        this.player = player;
        this.world = player.getWorld();
        this.sceneId = 3;
        this.enterSceneToken = 0;
        this.startWorldTime = this.world.getWorldTime();
    }


    public void initSceneLoading() {
        this.player.sendPacket(new SendPlayerEnterSceneNotify(this.player));
    }


    /**
     * Gets the time in seconds since the scene started.
     *
     * @return The time in seconds since the scene started.
     */
    public int getSceneTime() {
        return (int)(this.world.getWorldTime() - this.startWorldTime);
    }
}