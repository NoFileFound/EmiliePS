package org.genshinimpact.gameserver.game.world;

// Imports
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import org.genshinimpact.gameserver.enums.VisionType;
import org.genshinimpact.gameserver.game.Entity;
import org.genshinimpact.gameserver.game.avatar.AvatarEntity;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.SendPacket;

// Packets
import org.genshinimpact.gameserver.packets.send.avatar.SendAvatarDataNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendPlayerEnterSceneNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendSceneEntityAppearNotify;
import org.genshinimpact.gameserver.packets.send.scene.SendSceneEntityDisappearNotify;

public final class Scene {
    @Getter private final List<Player> players;
    @Getter private final int sceneId;
    @Getter private final World world;
    @Getter private final Map<Integer, Entity> sceneEntities;
    @Getter private boolean isPaused;
    private final long startWorldTime;

    /**
     * Creates a new Scene.
     * @param world The given world.
     */
    public Scene(World world, int sceneId) {
        this.world = world;
        this.players = new CopyOnWriteArrayList<>();
        this.sceneId = sceneId;
        this.sceneEntities = new ConcurrentHashMap<>();
        this.startWorldTime = this.world.getWorldTime();
    }

    /**
     * Adds a player to the scene.
     * @param player The player to add.
     */
    public synchronized void addPlayer(Player player) {
        if(this.players.contains(player)) {
            this.removePlayer(player);
        }

        if(player.getScene() != null) {
            player.getScene().removePlayer(player);
        }

        this.players.add(player);
        player.setScene(this);
        player.setSceneEnterToken(ThreadLocalRandom.current().nextInt(100, 32768));
        player.setSceneLoadState(SceneLoadState.INIT);
        player.getAntiCheatInfo().initAntiCheat();
        for(int avatarId : player.getAccount().getPlayerTeam().getCurrentTeam().getAvatars()) {
            player.getAccount().getPlayerTeam().getEntityAvatarList().add(new AvatarEntity(player.getAvatarStorage().get(avatarId), this));
        }

        player.sendPacket(new SendAvatarDataNotify(player));
        player.sendPacket(new SendPlayerEnterSceneNotify(player));
    }

    /**
     * Removes a player from the scene.
     * @param player The player to remove.
     */
    public synchronized void removePlayer(Player player) {
        ///  TODO: If player is in challenge remove him.

        this.players.remove(player);
        player.setScene(null);
        player.setSceneId(0);
        player.setSceneEnterToken(0);
        player.setSceneLoadState(SceneLoadState.NONE);
        player.getAntiCheatInfo().deInitAntiCheat();
        for(var entityEntry : player.getAccount().getPlayerTeam().getEntityAvatarList()) {
            this.removeEntity(entityEntry);
            player.getAccount().getPlayerTeam().getEntityAvatarList().remove(entityEntry);
        }

        ///  TODO: remove player's gadgets
        ///  TODO: Remove the scene from the world. if there are no players.
    }

    /**
     * Adds an entity to the scene.
     * @param entity The entity to add.
     */
    public synchronized void addEntity(Entity entity) {
        this.addEntity(entity, VisionType.VISION_BORN);
    }

    /**
     * Adds an entity to the scene.
     * @param entity The entity to add.
     * @param visionType The entity's vision type.
     */
    public synchronized void addEntity(Entity entity, VisionType visionType) {
        if(this.sceneEntities.containsKey(entity.getEntityId())) {
            return;
        }

        this.sceneEntities.put(entity.getEntityId(), entity);
        ///  TODO: CREATE THE ENTITY BEFORE SPAWN IT IN THE WORLD.
        this.sendPacket(new SendSceneEntityAppearNotify(entity, visionType));
    }

    /**
     * Removes entity from the scene.
     * @param entity The entity to remove.
     */
    public synchronized void removeEntity(Entity entity) {
        /// TODO: FINISH
    }

    /**
     * Gets the time in seconds since the scene started.
     *
     * @return The time in seconds since the scene started.
     */
    public int getSceneTime() {
        return (int)(this.world.getWorldTime() - this.startWorldTime);
    }

    /**
     * Sends a packet to every player in the current scene.
     * @param packet The packet to send.
     */
    public synchronized void sendPacket(SendPacket packet) {
        for(var player : this.players) {
            player.sendPacket(packet);
        }
    }

    /**
     * Spawns the player and other scene entities.
     * @param playerAvatarEntity The player's avatar entity.
     */
    public void sendSceneEntities(Entity playerAvatarEntity) {
        this.addEntity(playerAvatarEntity);
        for(var entityEntry : this.sceneEntities.values()) {
            if(entityEntry == playerAvatarEntity) {
                continue;
            }

            this.addEntity(entityEntry, VisionType.VISION_MEET);
        }
    }

    public void replaceEntity(Entity oldEntity, Entity newEntity) {
        this.sceneEntities.remove(oldEntity.getEntityId());
        this.sceneEntities.put(newEntity.getEntityId(), newEntity);
        this.sendPacket(new SendSceneEntityDisappearNotify(oldEntity, VisionType.VISION_REPLACE));
        this.sendPacket(new SendSceneEntityAppearNotify(newEntity, VisionType.VISION_REPLACE, oldEntity.getEntityId()));

    }
}