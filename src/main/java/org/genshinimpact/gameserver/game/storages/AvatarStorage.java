package org.genshinimpact.gameserver.game.storages;

// Imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.genshinimpact.gameserver.game.avatar.Avatar;
import org.genshinimpact.gameserver.game.player.Player;
import org.jetbrains.annotations.NotNull;

// Packets
import org.genshinimpact.gameserver.packets.send.avatar.SendAvatarAddNotify;

public final class AvatarStorage implements Iterable<Avatar> {
    private final Player player;
    private final Map<Integer, Avatar> avatarMap;
    private boolean isLoaded = false;

    /**
     * Creates a new avatar storage on the given player.
     * @param player The given player.
     */
    public AvatarStorage(Player player) {
        this.player = player;
        this.avatarMap = new HashMap<>();
        this.loadAvatars();
    }

    /**
     * Adds an avatar.
     * @param avatarId The avatar id.
     * @param addToTeam Should add the avatar to the player's active team.
     * @return True if avatar was added or else False.
     */
    public boolean addAvatar(int avatarId, boolean addToTeam) {
        if(this.avatarMap.containsKey(avatarId)) {
            return false;
        }

        var myAvatar = new Avatar(avatarId, this.player);
        this.avatarMap.put(avatarId, myAvatar);
        this.player.getAccount().getUnlockedAvatars().put(avatarId, myAvatar);
        if(addToTeam) {
            ///  TODO: ADD TO NEXT TEAM IF THE CURRENT TEAM IS FULL.
            this.player.getAccount().getPlayerTeam().getCurrentTeam().addAvatar(myAvatar);
        }

        if(this.player.isActive()) {
            this.player.sendPacket(new SendAvatarAddNotify(myAvatar, addToTeam));
        }
        return true;
    }

    /**
     * Adds an avatar.
     * @param avatarId The avatar id.
     * @return True if avatar was added or else False.
     */
    public boolean addAvatar(int avatarId) {
        return this.addAvatar(avatarId, false);
    }

    /**
     * Removes an avatar.
     * @param avatarId The avatar id.
     * @return True if avatar was removed or else False.
     */
    public boolean removeAvatar(int avatarId) {
        if(!this.avatarMap.containsKey(avatarId) || this.player.getAccount().getMainCharacterId() == avatarId) {
            return false;
        }

        this.avatarMap.remove(avatarId);
        this.player.getAccount().getUnlockedAvatars().remove(avatarId);
        if(this.player.getAccount().getProfileAvatarImageId() == avatarId) {
            this.player.getAccount().setProfileAvatarImageId(this.player.getAccount().getMainCharacterId());
            this.player.getAccount().setProfileAvatarCostumeImageId(0);
        }

        ///  TODO: REMOVE THE AVATAR FROM TEAM.
        /// TODO: SEND THE PACKETS TO AVATAR APPLY IN THE GAME
        return true;
    }

    /**
     * Gets avatar by avatarId.
     * @param avatarId The avatar id.
     * @return An avatar object.
     */
    public Avatar get(Integer avatarId) {
        return this.avatarMap.get(avatarId);
    }

    /**
     * Gets the total avatars on the player.
     * @return The total avatars that the player unlocked.
     */
    public int getTotalAvatars() {
        return this.avatarMap.size();
    }

    /**
     * Checks if the player has the given avatar.
     * @param avatarId The avatar's id.
     * @return True if he has it or else False.
     */
    public boolean hasAvatar(int avatarId) {
        return this.avatarMap.containsKey(avatarId);
    }

    /**
     * Loads the avatars from the database.
     */
    public void loadAvatars() {
        if(this.isLoaded) {
            return;
        }

        this.player.getAccount().getPlayerTeam().setPlayer(this.player);
        for(var avatarEntry : this.player.getAccount().getUnlockedAvatars().values()) {
            avatarEntry.setPlayer(this.player);
            this.avatarMap.put(avatarEntry.getAvatarId(), avatarEntry);
        }

        this.isLoaded = true;
    }

    /**
     * Returns an iterator over {@link Avatar}.
     * @return an {@link Iterator} over the {@link Avatar} objects in this storage.
     */
    @NotNull @Override public Iterator<Avatar> iterator() {
        return this.avatarMap.values().iterator();
    }
}