package org.genshinimpact.gameserver.game.avatar;

// Imports
import lombok.Getter;
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
import org.generated.protobuf.SceneWeaponInfoOuterClass.SceneWeaponInfo;
import org.generated.protobuf.VectorOuterClass.Vector;

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

    /**
     * Converts AvatarEntity into Proto object.
     * @return A protobuf of the current avatar entity.
     */
    @Override
    public SceneEntityInfo toProto() {
        return SceneEntityInfo.newBuilder()
            .addAnimatorParaList(AnimatorParameterValueInfoPair.newBuilder())
            .setAvatar(
                SceneAvatarInfo.newBuilder()
                    .setAvatarId(this.avatar.getAvatarId())
                    .setBornTime(this.avatar.getBornTime())
                    .setCostumeId(this.avatar.getCostumeId())
                    .setGuid(this.avatar.getAvatarGuid())
                    .setPeerId(this.avatar.getPlayer().getPeerId())
                    .setUid(this.avatar.getPlayer().getAccount().getId().intValue())
                    .setWearingFlycloakId(this.avatar.getFlyCloakId())
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
            .setLifeState(1)
            .setMotionInfo(MotionInfo.newBuilder().setPos(this.avatar.getPlayer().getAccount().getPlayerPosition().toProto()).setRot(this.avatar.getPlayer().getAccount().getPlayerRotation().toProto()).setSpeed(Vector.newBuilder().build()).setRefPos(Vector.newBuilder().build()).build())
            .build();
    }

    public AbilityControlBlock getAbilityControlBlock() {
        return AbilityControlBlock.newBuilder().build();
    }

    public SceneAvatarInfo getSceneAvatarInfo() {
        ///  TODO: REPLACE THAT INTO SendSceneTeamUpdateNotify because it is only one object.
        return SceneAvatarInfo.newBuilder().build();
    }
}

/// TODO: FINISH