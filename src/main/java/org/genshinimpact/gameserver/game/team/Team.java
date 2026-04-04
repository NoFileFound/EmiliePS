package org.genshinimpact.gameserver.game.team;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Transient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.gameserver.game.avatar.Avatar;
import org.genshinimpact.gameserver.game.avatar.AvatarEntity;
import org.genshinimpact.gameserver.game.player.Player;

// Protocol buffers
import org.generated.protobuf.AbilityControlBlockOuterClass.AbilityControlBlock;
import org.generated.protobuf.AvatarTeamOuterClass.AvatarTeam;

@Entity("Team")
@Getter
public final class Team {
    @Transient private final List<AvatarEntity> entityAvatarList;
    @Transient @Getter @Setter private TeamEntity entity;
    private final LinkedHashMap<Integer, TeamObject> teams;
    private int currentTeamIndex;
    private int currentCharacterIndex;

    /**
     * Creates a new Team object.
     */
    public Team() {
        this.entityAvatarList = Collections.synchronizedList(new ArrayList<>());
        this.teams = new LinkedHashMap<>();
        this.currentTeamIndex = 1;
        this.currentCharacterIndex = 0;
        for(int i = 1; i <= 4; i++) {
            this.teams.put(i, new TeamObject());
        }
    }

    /**
     * Gets the team's ability block.
     * @return The team's ability block.
     */
    public AbilityControlBlock getAbilityControlBlock() {
        return AbilityControlBlock.newBuilder().build();
    }

    /**
     * Gets the current avatar from the team.
     * @return The current avatar as an entity.
     */
    public AvatarEntity getCurrentAvatarEntity() {
        return this.entityAvatarList.get(this.currentCharacterIndex);
    }

    /**
     * Gets the current team.
     * @return The current team object.
     */
    public TeamObject getCurrentTeam() {
        return this.teams.get(this.currentTeamIndex);
    }


    @Entity("TeamInfo")
    @Getter
    public static final class TeamObject {
        @Setter private String name;
        private final List<Integer> avatars;

        /**
         * Creates a new instance of Team object.
         */
        public TeamObject() {
            this.name = "";
            this.avatars = new ArrayList<>(4);
        }

        /**
         * Creates a new instance of Team object.
         * @param name The team's nickname.
         * @param avatars The team's avatars,
         */
        public TeamObject(String name, List<Integer> avatars) {
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
            for(var avatarId : this.avatars) {
                avatarTeam.addAvatarGuidList(player.getAvatarStorage().get(avatarId).getAvatarGuid());
            }

            return avatarTeam.build();
        }
    }
}