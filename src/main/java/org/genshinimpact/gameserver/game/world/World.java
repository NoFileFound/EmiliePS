package org.genshinimpact.gameserver.game.world;

// Imports
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.gameserver.enums.EntityIdType;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.game.team.TeamEntity;
import org.genshinimpact.gameserver.game.player.Player;

public final class World {
    @Getter private final Player worldHost;
    @Getter private final Server server;
    @Getter private final List<Player> players;
    @Getter private final Int2ObjectMap<Scene> scenes;
    @Getter private final WorldEntity entity;
    @Getter private int worldLevel;
    @Getter private int worldType;
    @Getter @Setter private boolean isPaused;
    @Getter private boolean timeLocked;
    @Getter private long currentWorldTime;
    private int nextPeerId;
    private int nextEntityId;
    private long lastUpdateTime;

    /**
     * Creates a new instance of World.
     * @param worldHoster The player who created the world.
     */
    public World(Player worldHoster) {
        this.worldHost = worldHoster;
        this.server = worldHoster.getServer();
        this.players = Collections.synchronizedList(new ArrayList<>());
        this.scenes = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());
        this.entity = new WorldEntity(this);
        this.worldLevel = 8;
        this.worldType = 1;
        this.isPaused = false;
        this.timeLocked = false;
        this.lastUpdateTime = System.currentTimeMillis();
        this.nextPeerId = 0;
        this.nextEntityId = 0;
        this.currentWorldTime = worldHoster.getPlayerGameTime();
    }

    /**
     * Adds a player to the world.
     * @param player The player to add to the world.
     * @param sceneId The scene id.
     */
    public synchronized void addPlayer(Player player, int sceneId) {
        if(this.players.contains(player)) {
            this.removePlayer(player);
        }

        this.players.add(player);
        player.setWorld(this);
        player.setPeerId(this.getNextPeerId());
        player.getAccount().getPlayerTeam().setEntity(new TeamEntity(player, this));
        if(this.isMultiplayer()) {
            ///  TODO: MULTIPLAYER
        }

        player.setSceneId(sceneId);
        this.getSceneById(sceneId).addPlayer(player);
        if(this.isMultiplayer()) {
            ///  TODO: MULTIPLAYER
        }
    }

    /**
     * Adds a player to the world.
     * @param player The player to add to the world.
     */
    public synchronized void addPlayer(Player player) {
        this.addPlayer(player, 3);
    }

    /**
     * Removes a player from the world.
     * @param player The player to remove from the world.
     */
    public synchronized void removePlayer(Player player) {
        if(!this.players.contains(player)) {
            return;
        }

        this.players.remove(player);
        player.setWorld(null);
        player.setPeerId(-1);
        player.getAccount().getPlayerTeam().setEntity(null);
        this.getSceneById(player.getSceneId()).removePlayer(player);
        if(this.isMultiplayer()) {
            ///  TODO: MULTIPLAYER
        }

        if(player == this.worldHost) {
            ///  TODO: FIX
        }
    }

    /**
     * Checks if the world is multiplayer (has more than one player).
     * @return True if its multiplayer or else False.
     */
    public boolean isMultiplayer() {
        return this.players.size() > 1;
    }

    /**
     * Gets the next entity ID for the specified entity type.
     *
     * @param idType The entity type.
     * @return The next entity ID.
     */
    public synchronized int getNextEntityId(EntityIdType idType) {
        return(idType.getValue() << 24) + ++this.nextEntityId;
    }

    /**
     * Gets the current world peer id.
     * @return The world's peer id.
     */
    public synchronized int getNextPeerId() {
        return ++this.nextPeerId;
    }

    /**
     * Gets an associated scene by id.
     * @param sceneId The scene ID.
     * @return The scene.
     */
    public Scene getSceneById(int sceneId) {
        return this.scenes.computeIfAbsent(sceneId, id -> new Scene(this, id));
    }

    /**
     * Gets the current world timestamp.
     * @return The world's time in milliseconds.
     */
    public long getWorldTime() {
        if(!this.isPaused && !this.timeLocked) {
            var newUpdateTime = System.currentTimeMillis();
            this.currentWorldTime += (newUpdateTime - lastUpdateTime);
            this.lastUpdateTime = newUpdateTime;
        }

        return this.currentWorldTime;
    }
}