package org.genshinimpact.gameserver.game.team;

// Imports
import org.genshinimpact.gameserver.enums.EntityIdType;
import org.genshinimpact.gameserver.game.Entity;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.game.world.World;

public final class TeamEntity extends Entity {
    private final Player player;

    /**
     * Creates a new entity of Team.
     * @param player The player's object to create team's entity.
     */
    public TeamEntity(Player player, World world) {
        this.player = player;
        this.entityId = world.getNextEntityId(EntityIdType.TEAM);
    }
}