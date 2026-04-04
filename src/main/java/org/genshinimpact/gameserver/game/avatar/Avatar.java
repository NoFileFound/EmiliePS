package org.genshinimpact.gameserver.game.avatar;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Transient;
import lombok.Getter;
import lombok.Setter;
import org.generated.protobuf.PropValueOuterClass.PropValue;
import org.genshinimpact.gameserver.enums.LifeState;
import org.genshinimpact.gameserver.game.inventory.Item;
import org.genshinimpact.gameserver.game.player.Player;

// Protocol buffers
import org.generated.protobuf.AvatarFetterInfoOuterClass.AvatarFetterInfo;
import org.generated.protobuf.AvatarInfoOuterClass.AvatarInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity("Avatar")
@Getter
public final class Avatar {
    @Id private final int avatarId;
    @Transient private Player player;
    @Transient private long avatarGuid;
    private final int bornTime;
    @Setter private int costumeId = 0;
    @Setter private int flyCloakId = 140001;
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
            .setCoreProudSkillLevel(0)
            .setCostumeId(this.costumeId)
            .setFetterInfo(AvatarFetterInfo.newBuilder().setExpLevel(1).setExpNumber(0).build())
            .setGuid(this.avatarGuid)
            .setLifeState(LifeState.LIFE_ALIVE.getValue())
            .setSkillDepotId(12201)
            .setWearingFlycloakId(this.flyCloakId)
            .putAllFightPropMap(new HashMap<Integer, Float>() {{
                put(1, 1897.8414f);
                put(4, 74.69876f);
                put(7, 119.40633f);
                put(20, 0.05f);
                put(21, 0f);
                put(22, 0.5f);
                put(23, 1.0f);
                put(28, 100.0f);
                put(73, 60.0f);
                put(2000, 1897.8414f);
                put(2001, 74.69876f);
                put(2002, 119.40633f);
                put(3046, 1.0f);
                put(1010, 664.2445f);
            }})
            .putAllPropMap(Map.of(
                    4001, PropValue.newBuilder().setType(4001).setIval(12).setVal(12).build()
            ))
            .putAllProudSkillExtraLevelMap(new HashMap<Integer, Integer>() {{
                put(3232, 3);
            }})
            /// TODO: .putAllSkillMap()
            .putAllSkillLevelMap(new HashMap<Integer, Integer>() {{
                put(11221, 1);
                put(11222, 1);
                put(11225, 1);
            }})
            .addAllEquipGuidList(List.of(this.getWeapon().getItemGuid()))
            .addAllInherentProudSkillList(List.of(1222301, 1222501))
            .addAllTalentIdList(List.of(151))
            .build();
    }

    public Item getWeapon() {
        if(this.weaponItem == null) {
            this.weaponItem = new Item(this.player);
        }


        return this.weaponItem;
    }
}

/*
    TrialAvatarInfo trial_avatar_info = 9;
    AvatarExpeditionState expedition_state = 16;
    bool is_focus = 18;
    repeated uint32 team_resonance_list = 20;
    repeated AvatarEquipAffixInfo equip_affix_list = 22;
    repeated uint32 pending_promote_reward_list = 24;
    AvatarExcelInfo excel_info = 26;
    uint32 anim_hash = 27;
*/