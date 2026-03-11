package org.genshinimpact.gameserver.packets.send.scene;

import org.generated.protobuf.AbilityControlBlockOuterClass.AbilityControlBlock;
import org.generated.protobuf.AbilitySyncStateInfoOuterClass.AbilitySyncStateInfo;
import org.generated.protobuf.PlayerEnterSceneInfoNotifyOuterClass.PlayerEnterSceneInfoNotify;
import org.genshinimpact.gameserver.packets.OutboundPacket;

public class SendPlayerEnterSceneInfoNotify extends OutboundPacket {

    public SendPlayerEnterSceneInfoNotify(int sceneToken) {
        super(214);

        AbilitySyncStateInfo empty = AbilitySyncStateInfo.newBuilder().build();

        PlayerEnterSceneInfoNotify.Builder proto =
                PlayerEnterSceneInfoNotify.newBuilder()
                        .setCurAvatarEntityId(0)
                        .setEnterSceneToken(sceneToken);

        proto.setTeamEnterInfo(
                PlayerEnterSceneInfoNotify.TeamEnterSceneInfo.newBuilder()
                        .setTeamEntityId(150995833) // 150995833
                        .setTeamAbilityInfo(empty)
                        .setAbilityControlBlock(AbilityControlBlock.newBuilder().build()));
        proto.setMpLevelEntityInfo(
                PlayerEnterSceneInfoNotify.MPLevelEntityInfo.newBuilder()
                        .setEntityId(184550274) // 184550274
                        .setAuthorityPeerId(0)
                        .setAbilityInfo(empty));

        this.setData(proto.build().toByteArray());
    }
}
