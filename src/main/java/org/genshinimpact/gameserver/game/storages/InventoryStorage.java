package org.genshinimpact.gameserver.game.storages;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.genshinimpact.gameserver.game.inventory.Item;
import org.genshinimpact.gameserver.game.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class InventoryStorage implements Iterable<Item> {
    private final Player player;
    private final Long2ObjectMap<Item> inventoryMap;

    public InventoryStorage(Player player) {
        this.player = player;
        this.inventoryMap = new Long2ObjectOpenHashMap<>();
    }


    @NotNull
    @Override
    public Iterator<Item> iterator() {
        return this.inventoryMap.values().iterator();
    }
}

/// TODO: FINISH