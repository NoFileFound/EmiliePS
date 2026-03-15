package org.genshinimpact.gameserver.game.avatar;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Transient;
import lombok.Getter;
import org.generated.protobuf.SceneEntityInfoOuterClass;
import org.genshinimpact.gameserver.game.player.Player;

// Protocol buffers
import org.generated.protobuf.AvatarInfoOuterClass.AvatarInfo;

@Entity
public class Avatar {
    @Id @Getter private final int avatarId;
    @Transient @Getter private long guid;

    public Avatar(int avatarId) {
        this.avatarId = avatarId;
    }

    public void loadAvatar(Player player) {
        this.guid = player.getNextGuid();
    }

    public AvatarInfo toProto() {
        AvatarInfo.Builder avatarInfo =
                AvatarInfo.newBuilder()
                        .setAvatarId(this.getAvatarId())
                        .setGuid(this.getGuid())
                        .setBornTime((int)System.currentTimeMillis() / 1000)
                        .setLifeState(1);

        return avatarInfo.build();
    }
}