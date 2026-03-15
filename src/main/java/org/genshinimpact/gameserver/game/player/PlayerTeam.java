package org.genshinimpact.gameserver.game.player;

// Imports
import dev.morphia.annotations.Entity;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import org.genshinimpact.gameserver.game.avatar.Avatar;

// Protocol buffers
import org.generated.protobuf.AvatarTeamOuterClass.AvatarTeam;

@Entity
@Getter
public final class PlayerTeam {
    @Setter private String name;
    private final List<Integer> avatars;

    /**
     * Creates a new instance of Team object.
     */
    public PlayerTeam() {
        this.name = "";
        this.avatars = new ArrayList<>(4);
    }

    /**
     * Creates a new instance of Team object.
     * @param name The team's nickname.
     * @param avatars The team's avatars,
     */
    public PlayerTeam(String name, List<Integer> avatars) {
        this.name = name;
        this.avatars = avatars;
    }

    /**
     * Adds an avatar to the current team.
     */
    public void addAvatar(Avatar avatar) {
        if(this.avatars.contains(avatar.getAvatarId())) {
            return;
        }

        this.avatars.add(avatar.getAvatarId());
    }

    /**
     * Removes an avatar from the current team.
     * @param slotId The avatar's position id in the team.
     */
    public void removeAvatar(int slotId) {
        this.avatars.remove(slotId);
    }

    /**
     * Converts the Team into Proto object.
     * @param player The player's object.
     * @return A protobuf of the current team.
     */
    public AvatarTeam toProto(Player player) {
        var avatarTeam = AvatarTeam.newBuilder().setTeamName(this.name);
        for(Integer avatarId : this.avatars) {
            avatarTeam.addAvatarGuidList(player.getPlayerIdentity().getAvatars().get(avatarId).getGuid());
        }

        return avatarTeam.build();
    }
}