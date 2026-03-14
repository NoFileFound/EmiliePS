package org.genshinimpact.gameserver.game.world;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.gameserver.game.player.Player;

public class World {
    @Getter private final Object2ObjectMap<Player.PlayerKey, Player> players;
    private final Player worldOwner;
    @Setter private boolean isPaused;

    public World(Player worldOwner) {
        this.players = new Object2ObjectOpenHashMap<>();
        this.worldOwner = worldOwner;
    }
}