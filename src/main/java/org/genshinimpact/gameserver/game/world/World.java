package org.genshinimpact.gameserver.game.world;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.gameserver.game.player.Player;

public class World {
    @Getter @Setter private boolean isPaused;
    @Getter private int worldLevel;
    @Getter private int worldType;
    @Getter private final Player worldHost;
    @Getter private final Object2ObjectMap<Long, Player> players;
    private int nextPeerId = 0;
    @Getter private long currentWorldTime;
    private long lastUpdateTime;
    @Getter private boolean timeLocked;

    public World(Player worldOwner) {
        this.players = new Object2ObjectOpenHashMap<>();
        this.worldHost = worldOwner;
        this.worldLevel = 1;
        worldOwner.setPeerId(this.getNextPeerId());

        this.lastUpdateTime = System.currentTimeMillis();
        this.currentWorldTime = worldOwner.getPlayerGameTime();
    }

    public int getNextPeerId() {
        return ++this.nextPeerId;
    }

    public long getWorldTime() {
        if (!this.isPaused && !this.timeLocked) {
            var newUpdateTime = System.currentTimeMillis();
            this.currentWorldTime += (newUpdateTime - lastUpdateTime);
            this.lastUpdateTime = newUpdateTime;
        }

        return this.currentWorldTime;
    }
}