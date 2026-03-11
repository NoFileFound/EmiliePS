package org.genshinimpact.gameserver.packets.send.scene;

import org.generated.protobuf.SyncScenePlayTeamEntityNotifyOuterClass.SyncScenePlayTeamEntityNotify;
import org.genshinimpact.gameserver.packets.OutboundPacket;

public class SendSyncScenePlayTeamEntityNotify extends OutboundPacket {

    public SendSyncScenePlayTeamEntityNotify() {
        super(3333);

        SyncScenePlayTeamEntityNotify proto = SyncScenePlayTeamEntityNotify.newBuilder().setSceneId(3).build();
        this.setData(proto.toByteArray());
    }
}
