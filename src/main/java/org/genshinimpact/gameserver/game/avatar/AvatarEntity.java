package org.genshinimpact.gameserver.game.avatar;

// Imports
import lombok.Getter;
import org.generated.protobuf.SceneWeaponInfoOuterClass.SceneWeaponInfo;
import org.genshinimpact.gameserver.enums.EntityIdType;
import org.genshinimpact.gameserver.game.Entity;
import org.genshinimpact.gameserver.game.world.Scene;

// Protocol buffers
import org.generated.protobuf.AbilityControlBlockOuterClass.AbilityControlBlock;
import org.generated.protobuf.AbilitySyncStateInfoOuterClass.AbilitySyncStateInfo;
import org.generated.protobuf.AnimatorParameterValueInfoPairOuterClass.AnimatorParameterValueInfoPair;
import org.generated.protobuf.EntityAuthorityInfoOuterClass.EntityAuthorityInfo;
import org.generated.protobuf.EntityClientDataOuterClass.EntityClientData;
import org.generated.protobuf.EntityRendererChangedInfoOuterClass.EntityRendererChangedInfo;
import org.generated.protobuf.MotionInfoOuterClass.MotionInfo;
import org.generated.protobuf.SceneAvatarInfoOuterClass.SceneAvatarInfo;
import org.generated.protobuf.SceneEntityInfoOuterClass.SceneEntityInfo;
import org.generated.protobuf.VectorOuterClass.Vector;

import java.util.HashMap;
import java.util.List;

public final class AvatarEntity extends Entity {
    @Getter private final Avatar avatar;

    /**
     * Creates a new entity of Avatar object.
     * @param avatar The avatar.
     * @param scene The scene.
     */
    public AvatarEntity(Avatar avatar, Scene scene) {
        this.avatar = avatar;
        this.entityId = scene.getWorld().getNextEntityId(EntityIdType.AVATAR);
    }

    @Override
    public SceneEntityInfo toProto() {
        return SceneEntityInfo.newBuilder()
            .addAnimatorParaList(AnimatorParameterValueInfoPair.newBuilder())
            .setAvatar(
                SceneAvatarInfo.newBuilder()
                        .addEquipIdList(11101)
                    .setAvatarId(this.avatar.getAvatarId())
                    .setGuid(this.avatar.getAvatarGuid())
                    .setPeerId(this.avatar.getPlayer().getPeerId())
                    .setUid(this.avatar.getPlayer().getAccount().getId().intValue())
                    .addAllTalentIdList(List.of(151))
                    .setCoreProudSkillLevel(0)
                    .putAllSkillLevelMap(new HashMap<Integer, Integer>() {{
                        put(11221, 1);
                        put(11222, 1);
                        put(11225, 1);
                    }})
                    .setSkillDepotId(12201)
                    .addAllInherentProudSkillList(List.of(1222301, 1222501))
                    /// TODO .putAllProudSkillExtraLevelMap(this.avatar.getProudSkillBonusMap())
                    .addAllTeamResonanceList(List.of(10701))
                    .setWearingFlycloakId(this.avatar.getFlyCloakId())
                    .setCostumeId(this.avatar.getCostumeId())
                    .setBornTime(this.avatar.getBornTime())
                    .setWeapon(SceneWeaponInfo.newBuilder()
                            .setEntityId(this.avatar.getWeapon().getItemEntity().getEntityId())
                            .setGuid(this.avatar.getWeapon().getItemGuid())
                            .setItemId(14101)
                            .setLevel(1)
                            .setPromoteLevel(0)
                            .setGadgetId(50014101)
                            .setRendererChangedInfo(EntityRendererChangedInfo.newBuilder().build())
                            .setAbilityInfo(AbilitySyncStateInfo.newBuilder().build())
                            .build())
                    .build())
            .setEntityAuthorityInfo(
                EntityAuthorityInfo.newBuilder()
                    .setAbilityInfo(AbilitySyncStateInfo.newBuilder())
                    .setRendererChangedInfo(EntityRendererChangedInfo.newBuilder())
                    .setAiInfo(EntityAuthorityInfo.SceneEntityAiInfo.newBuilder().setIsAiOpen(true).setBornPos(Vector.newBuilder()))
                    .setBornPos(Vector.newBuilder())
                    .build())
            .setEntityClientData(EntityClientData.newBuilder())
            .setEntityId(this.entityId)
            .setEntityType(SceneEntityInfo.ProtEntityType.PROT_ENTITY_AVATAR)
            .setLastMoveSceneTimeMs(0)
            .setLastMoveReliableSeq(0)
                .setMotionInfo(MotionInfo.newBuilder().setPos(this.avatar.getPlayer().getAccount().getPlayerPosition().toProto()).setRot(Vector.newBuilder().build()).setSpeed(Vector.newBuilder().build()).setRefPos(Vector.newBuilder().build()).build())
            .setLifeState(1) ///  TODO
            .build();
    }

    public AbilityControlBlock getAbilityControlBlock() {
        return AbilityControlBlock.newBuilder().build();
    }

    public SceneAvatarInfo getSceneAvatarInfo() {
        return SceneAvatarInfo.newBuilder().build();
    }
}