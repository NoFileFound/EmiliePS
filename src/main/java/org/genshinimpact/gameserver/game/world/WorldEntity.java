package org.genshinimpact.gameserver.game.world;

// Imports
import org.genshinimpact.gameserver.enums.EntityIdType;
import org.genshinimpact.gameserver.game.Entity;

public final class WorldEntity extends Entity {
    private final World world;

    /**
     * Creates a new entity of World object.
     * @param world The world.
     */
    public WorldEntity(World world) {
        this.world = world;
        this.entityId = world.getNextEntityId(EntityIdType.MPLEVEL);
    }
}