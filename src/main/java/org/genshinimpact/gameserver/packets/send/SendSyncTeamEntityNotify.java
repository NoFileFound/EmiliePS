package org.genshinimpact.gameserver.packets.send;

import org.generated.protobuf.SyncTeamEntityNotifyOuterClass;
import org.genshinimpact.gameserver.packets.OutboundPacket;

public class SendSyncTeamEntityNotify extends OutboundPacket {

    public SendSyncTeamEntityNotify() {
        super(317);

        SyncTeamEntityNotifyOuterClass.SyncTeamEntityNotify.Builder proto = SyncTeamEntityNotifyOuterClass.SyncTeamEntityNotify.newBuilder().setSceneId(13);
        this.setData(proto.build().toByteArray());
    }
}
