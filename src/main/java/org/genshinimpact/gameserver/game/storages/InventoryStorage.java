package org.genshinimpact.gameserver.game.storages;

// Imports
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Iterator;
import org.genshinimpact.gameserver.game.inventory.Item;
import org.genshinimpact.gameserver.game.player.Player;
import org.jetbrains.annotations.NotNull;

public final class InventoryStorage implements Iterable<Item> {
    private final Player player;
    private final Long2ObjectMap<Item> inventoryMap;
    private boolean isLoaded = false;

    /**
     * Creates a new inventory storage on the given player.
     * @param player The given player.
     */
    public InventoryStorage(Player player) {
        this.player = player;
        this.inventoryMap = new Long2ObjectOpenHashMap<>();
        this.loadInventory();
    }

    /**
     * Loads the items from the database.
     */
    public void loadInventory() {
        if(this.isLoaded) {
            return;
        }

        ///  TODO: FINISH
        this.isLoaded = true;
    }

    /**
     * Returns an iterator over {@link Item}.
     * @return an {@link Iterator} over the {@link Item} objects in this storage.
     */
    @NotNull @Override public Iterator<Item> iterator() {
        return this.inventoryMap.values().iterator();
    }
}