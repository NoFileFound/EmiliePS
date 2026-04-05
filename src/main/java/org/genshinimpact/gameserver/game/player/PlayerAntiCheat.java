package org.genshinimpact.gameserver.game.player;

// Imports
import org.genshinimpact.gameserver.game.world.Scene;
import org.genshinimpact.gameserver.game.world.World;

public final class PlayerAntiCheat {
    public static final int antiCheatVersion = 0;
    private final Player player;
    private World world;
    private Scene scene;
    private AntiCheatStatus acStatus;

    /**
     * Creates a new instance of PlayerAntiCheat.
     * @param player The player to initialize the anticheat.
     */
    public PlayerAntiCheat(Player player) {
        this.player = player;
        this.acStatus = AntiCheatStatus.INACTIVE;
    }

    /**
     * Initializes the anticheat when player joins a world.
     */
    public void initAntiCheat() {
        this.world = player.getWorld();
        this.scene = player.getScene();
        this.acStatus = AntiCheatStatus.INIT;
    }

    /**
     * Deinitializes the anticheat when player leaves a world.
     */
    public void deInitAntiCheat() {
        this.world = null;
        this.scene = null;
        this.acStatus = AntiCheatStatus.INACTIVE;
    }

    /**
     * Checks if there are any issues with the player on the scene.
     * @param sceneId The scene id.
     * @return True if there are no problems or else False.
     */
    public boolean checkEnterScene(int sceneId) {
        if(this.scene == null || this.world == null) {
            return false;
        }

        return this.scene.getSceneId() == sceneId;
    }

    /**
     * Changes the state of the anticheat.
     * @param status The status to change.
     */
    public void setACStatus(AntiCheatStatus status) {
        this.acStatus = status;
    }

    public enum AntiCheatStatus {
        INACTIVE,
        INIT,
        PASSED_TO_THE_MOON_ENTER_SCENE,
        ACTIVATED,
        FOUND_SUSPECT_ELEMENT,
        EXCLUDED
    }
}