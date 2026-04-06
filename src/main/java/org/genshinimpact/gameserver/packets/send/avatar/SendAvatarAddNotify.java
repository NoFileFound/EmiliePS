package org.genshinimpact.gameserver.packets.send.avatar;

// Imports
import org.genshinimpact.gameserver.game.avatar.Avatar;
import org.genshinimpact.gameserver.packets.SendPacket;

// Protocol buffers
import org.generated.protobuf.AvatarAddNotifyOuterClass.AvatarAddNotify;

public final class SendAvatarAddNotify implements SendPacket {
    private final byte[] data;

    public SendAvatarAddNotify(Avatar avatar, boolean addedToTeam) {
        var proto =
            AvatarAddNotify.newBuilder()
                .setAvatar(avatar.toProto())
                .setIsInTeam(addedToTeam)
                .build();

        this.data = proto.toByteArray();
    }

    @Override
    public int getCode() {
        return org.genshinimpact.gameserver.packets.PacketIdentifiers.Send.AvatarAddNotify;
    }

    @Override
    public byte[] getPacket() {
        return this.data;
    }
}