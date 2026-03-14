package org.genshinimpact.gameserver.game.avatar;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.Getter;

@Entity("avatars")
@Getter
public class Avatar {
    @Id private final int avatarId;

    public Avatar(int avatarId) {
        this.avatarId = avatarId;
    }
}