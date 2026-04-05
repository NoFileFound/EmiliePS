package org.genshinimpact.gameserver.packets.send.social;

// Imports
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.game.player.PlayerBirthday;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.BirthdayOuterClass.Birthday;
import org.generated.protobuf.SetPlayerBirthdayRspOuterClass.SetPlayerBirthdayRsp;

public final class SendSetPlayerBirthdayRsp implements SendPacket {
    private final byte[] data;

    public SendSetPlayerBirthdayRsp(Retcode retcode) {
        var proto =
            SetPlayerBirthdayRsp.newBuilder()
                .setRetcode(retcode.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    public SendSetPlayerBirthdayRsp(PlayerBirthday birthday) {
        var proto =
            SetPlayerBirthdayRsp.newBuilder()
                .setBirthday(Birthday.newBuilder().setDay(birthday.getDay()).setMonth(birthday.getMonth()).build())
                .setRetcode(Retcode.RET_SUCC.getValue())
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.SetPlayerBirthdayRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}