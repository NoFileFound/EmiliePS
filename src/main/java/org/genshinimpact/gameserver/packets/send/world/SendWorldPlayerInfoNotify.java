package org.genshinimpact.gameserver.packets.send.world;

import org.generated.protobuf.WorldPlayerInfoNotifyOuterClass.WorldPlayerInfoNotify;
import org.genshinimpact.gameserver.packets.OutboundPacket;

public class SendWorldPlayerInfoNotify extends OutboundPacket {

    public SendWorldPlayerInfoNotify() {
        super(3116);

        WorldPlayerInfoNotify proto = WorldPlayerInfoNotify.newBuilder().build();

        this.setData(proto.toByteArray());
    }
}
