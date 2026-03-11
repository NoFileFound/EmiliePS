package org.genshinimpact.gameserver.packets.send.player;

import org.generated.protobuf.HostPlayerNotifyOuterClass.HostPlayerNotify;
import org.genshinimpact.gameserver.packets.OutboundPacket;

public class SendHostPlayerNotify extends OutboundPacket {

    public SendHostPlayerNotify() {
        super(312);

        HostPlayerNotify proto =
                HostPlayerNotify.newBuilder()
                        .setHostUid(2)
                        .setHostPeerId(0)
                        .build();

        this.setData(proto.toByteArray());
    }
}
