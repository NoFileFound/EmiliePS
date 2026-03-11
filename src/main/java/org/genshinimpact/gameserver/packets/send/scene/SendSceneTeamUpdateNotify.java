package org.genshinimpact.gameserver.packets.send.scene;

import org.generated.protobuf.AbilityControlBlockOuterClass;
import org.generated.protobuf.AbilitySyncStateInfoOuterClass;
import org.generated.protobuf.SceneEntityInfoOuterClass;
import org.generated.protobuf.SceneTeamUpdateNotifyOuterClass.SceneTeamUpdateNotify;
import org.genshinimpact.gameserver.packets.OutboundPacket;

public class SendSceneTeamUpdateNotify extends OutboundPacket {

    public SendSceneTeamUpdateNotify() {
        super(1775);
        var proto = SceneTeamUpdateNotify.newBuilder().setIsInMp(false);

        var avatarProto =
                SceneTeamUpdateNotify.SceneTeamAvatar.newBuilder()
                        .setPlayerUid(2)
                        .setAvatarGuid(0)
                        .setSceneId(3)
                        //.setEntityId(100002)
                        .setSceneEntityInfo(SceneEntityInfoOuterClass.SceneEntityInfo.newBuilder().build())
                        .setWeaponGuid(0)
                        //.setWeaponEntityId(1)
                        .setIsPlayerCurAvatar(true)
                        .setIsOnScene(true)
                        .setAvatarAbilityInfo(AbilitySyncStateInfoOuterClass.AbilitySyncStateInfo.newBuilder())
                        .setWeaponAbilityInfo(AbilitySyncStateInfoOuterClass.AbilitySyncStateInfo.newBuilder())
                        .setAbilityControlBlock(AbilityControlBlockOuterClass.AbilityControlBlock.newBuilder().build());

        proto.addSceneTeamAvatarList(avatarProto);

        this.setData(proto.build().toByteArray());
    }
}
