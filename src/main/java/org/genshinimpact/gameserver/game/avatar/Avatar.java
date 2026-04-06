package org.genshinimpact.gameserver.game.avatar;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Transient;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.gameserver.enums.LifeState;
import org.genshinimpact.gameserver.game.inventory.Item;
import org.genshinimpact.gameserver.game.player.Player;

// Protocol buffers
import org.generated.protobuf.AvatarInfoOuterClass.AvatarInfo;

@Entity("Avatar")
@Getter
public final class Avatar {
    @Id private final int avatarId;
    @Transient private Player player;
    @Transient private long avatarGuid;
    private final int bornTime;
    @Setter private int flyCloakId = 0;
    @Setter private int costumeId = 0;
    @Transient private Item weaponItem;

    /**
     * Creates a new instance of avatar.
     * @param avatarId The avatar id.
     * @param avatarAuthor The player who unlocked the avatar.
     */
    public Avatar(int avatarId, Player avatarAuthor) {
        this.avatarId = avatarId;
        this.avatarGuid = avatarAuthor.getNextGuid();
        this.player = avatarAuthor;
        this.bornTime = (int)(System.currentTimeMillis() / 1000);
    }

    /**
     * Sets the avatar's owner.
     * @param player The avatar's owner.
     */
    public void setPlayer(Player player) {
        this.player = player;
        this.avatarGuid = player.getNextGuid();
    }

    /**
     * @return The Avatar as proto object.
     */
    public AvatarInfo toProto() {
        return AvatarInfo
            .newBuilder()
                .setAvatarId(this.avatarId)
                .setAvatarType(AvatarType.NORMAL.getValue())
                .setBornTime(this.bornTime)
                .setCostumeId(this.costumeId)
                .setGuid(this.avatarGuid)
                .setLifeState(LifeState.LIFE_ALIVE.getValue())
                .setWearingFlycloakId(this.flyCloakId)
                .build();
    }







    public Item getWeapon() {
        ///  TODO: FINISH
        if(this.weaponItem == null) {
            this.weaponItem = new Item(this.player);
        }

        return this.weaponItem;
    }

    public int getLevel() {
        return 60;
    }
}

/*
    SkillDepot:
    MALE -> 504
    FEMALE -> 704
 */

/// TODO: FINISH