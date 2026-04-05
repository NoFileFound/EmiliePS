package org.genshinimpact.gameserver.game.inventory;

import lombok.Getter;
import org.genshinimpact.gameserver.game.player.Player;

public class Item {
    @Getter private long itemGuid;
    @Getter private ItemEntity itemEntity;

    public Item(Player itemAuthor) {
        this.itemGuid = itemAuthor.getNextGuid();
        this.itemEntity = new ItemEntity(itemAuthor.getWorld());
    }
}


/// TODO: FINISH