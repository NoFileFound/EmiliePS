package org.genshinimpact.gameserver.packets.send.player;

import org.generated.protobuf.PlayerGameTimeNotifyOuterClass.PlayerGameTimeNotify;
import org.genshinimpact.gameserver.packets.OutboundPacket;

public class SendPlayerGameTimeNotify extends OutboundPacket {

    public SendPlayerGameTimeNotify() {
        super(131);

        this.setData(PlayerGameTimeNotify.newBuilder().setGameTime(0).setUid(2).build().toByteArray());
    }
}