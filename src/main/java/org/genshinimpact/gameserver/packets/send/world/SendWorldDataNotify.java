package org.genshinimpact.gameserver.packets.send.world;

import org.generated.protobuf.PropValueOuterClass.PropValue;
import org.generated.protobuf.WorldDataNotifyOuterClass.WorldDataNotify;
import org.genshinimpact.gameserver.packets.OutboundPacket;

public class SendWorldDataNotify extends OutboundPacket {

    public SendWorldDataNotify() {
        super(3308);

        int worldLevel = 1;
        int isMp = 0;

        WorldDataNotify proto =
                WorldDataNotify.newBuilder()
                        .putWorldPropMap(
                                1, PropValue.newBuilder().setType(1).setIval(worldLevel).setVal(worldLevel).build())
                        .putWorldPropMap(
                                2, PropValue.newBuilder().setType(2).setIval(isMp).setVal(isMp).build())
                        .build();

        this.setData(proto.toByteArray());
    }
}
