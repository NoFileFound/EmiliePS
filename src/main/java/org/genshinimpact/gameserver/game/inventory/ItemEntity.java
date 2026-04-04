package org.genshinimpact.gameserver.game.inventory;

import org.genshinimpact.gameserver.enums.EntityIdType;
import org.genshinimpact.gameserver.game.Entity;
import org.genshinimpact.gameserver.game.world.World;

public class ItemEntity extends Entity {

    public ItemEntity(World world) {
        this.entityId = world.getNextEntityId(EntityIdType.WEAPON);
    }
}