package org.genshinimpact.gameserver.packets.send.avatar;

// Imports
import static org.genshinimpact.gameserver.enums.Retcode.RET_SUCC;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.ChangeAvatarRspOuterClass.ChangeAvatarRsp;

public final class SendChangeAvatarRsp implements SendPacket {
    private final byte[] data;

    public SendChangeAvatarRsp(long avatarGuid, int skillId) {
        var proto =
            ChangeAvatarRsp.newBuilder()
                .setCurGuid(avatarGuid)
                .setRetcode(RET_SUCC.getValue())
                .setSkillId(skillId)
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.ChangeAvatarRsp;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}