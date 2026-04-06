package org.genshinimpact.gameserver.game;

// Imports
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.genshinimpact.database.DBUtils;
import org.genshinimpact.gameserver.connection.ClientHandler;
import org.genshinimpact.gameserver.connection.ClientSession;
import org.genshinimpact.gameserver.connection.kcp.KcpSession;
import org.genshinimpact.gameserver.enums.FriendEnterHomeOption;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.packets.BadPacketException;
import org.genshinimpact.gameserver.packets.RecvPacket;
import org.genshinimpact.gameserver.packets.PacketManager;
import org.kcp.ChannelConfig;
import org.kcp.KcpServer;
import org.kcp.Ukcp;

// Protocol buffers
import org.generated.protobuf.ChatInfoOuterClass.ChatInfo;
import org.generated.protobuf.FriendBriefOuterClass.FriendBrief;
import org.generated.protobuf.ProfilePictureOuterClass.ProfilePicture;
import org.generated.protobuf.SocialDetailOuterClass.SocialDetail;
import org.generated.protobuf.SocialShowAvatarInfoOuterClass.SocialShowAvatarInfo;

public final class Server extends KcpServer {
    @Getter private final PacketManager packetManager;
    @Getter private final Object2ObjectMap<Long, Player> players;
    @Getter private final Map<Integer, Map<Integer, List<ChatInfo>>> chatMessages;
    private final Object2ObjectMap<Ukcp, KcpSession> sessions;

    /**
     * Initializes the game server.
     */
    public Server() {
        this.sessions = new Object2ObjectOpenHashMap<>();
        this.players = new Object2ObjectOpenHashMap<>();
        this.chatMessages = new HashMap<>();
        this.packetManager = new PacketManager(RecvPacket.class);

        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.nodelay(true, 20, 2, true);
        channelConfig.setMtu(1400);
        channelConfig.setSndwnd(256);
        channelConfig.setRcvwnd(256);
        channelConfig.setTimeoutMillis(30 * 1000);
        channelConfig.setUseConvChannel(true);
        channelConfig.setAckNoDelay(false);
        this.init(new ClientHandler(this), channelConfig, new InetSocketAddress("127.0.0.1", 8882));
    }

    /**
     * Adds a new session.
     * @param ukcp The session's UKCP.
     * @param session The session's instance.
     */
    public void addSession(Ukcp ukcp, ClientSession session) {
        this.sessions.put(ukcp, session);
    }

    /**
     * Removes a session.
     * @param ukcp The session's UKCP.
     */
    public void removeSession(Ukcp ukcp) {
        if(this.sessions.containsKey(ukcp)) {
            this.sessions.get(ukcp).onClose();
            this.sessions.remove(ukcp);
        }
    }

    /**
     * Handles when a client session receives a data.
     * @param ukcp The session's UKCP.
     * @param data The session's data.
     * @throws BadPacketException The packet is invalid.
     */
    public void sessionReceiveData(Ukcp ukcp, ByteBuf data) throws Exception {
        if(this.sessions.containsKey(ukcp)) {
            this.sessions.get(ukcp).onReceive(data);
        }
    }

    /**
     * Gets the player's friend information in the game.
     * @param targetUid The target uid.
     * @return A FriendBrief protobuf object.
     */
    public FriendBrief getPlayerBriefInfo(long targetUid) {
        if(targetUid == 0) {
            return FriendBrief.newBuilder()
                .setFriendEnterHomeOption(FriendEnterHomeOption.FRIEND_ENTER_HOME_OPTION_REFUSE.getValue())
                .setIsChatNoDisturb(false)
                .setIsGameSource(true)
                .setIsMpModeAvailable(false)
                .setIsPsnSource(false)
                .setLastActiveTime((int)(System.currentTimeMillis() / 1000))
                .setLevel(1)
                .setNameCardId(210092)
                .setNickname("Emilie")
                .setOnlineId("Emilie")
                .setOnlineState(1)
                .setParam(1)
                .setPlatformType(0)
                .setProfilePicture(ProfilePicture.newBuilder().setAvatarId(10000052).setCostumeId(0).build())
                .setSignature("The server's console.")
                .setUid(0)
                .setWorldLevel(0)
                .build();
        }

        var myPlayer = this.players.get(targetUid);
        var myAccount = (myPlayer != null ? myPlayer.getAccount() : DBUtils.findAccountById(targetUid));
        if(myAccount == null) {
            return null;
        }

        List<SocialShowAvatarInfo> socialShowAvatarInfoList = new ArrayList<>();
        if(myAccount.isShowProfileAvatars()) {
            for(int avatarId : myAccount.getUnlockedAvatarsProfileShown()) {
                var avatarEntry = myAccount.getUnlockedAvatars().get(avatarId);
                socialShowAvatarInfoList.add(SocialShowAvatarInfo.newBuilder().setAvatarId(avatarId).setLevel(avatarEntry.getLevel()).setCostumeId(avatarEntry.getCostumeId()).build());
            }
        }

        return FriendBrief.newBuilder()
            .addAllShowAvatarInfoList(socialShowAvatarInfoList)
            /// TODO: .setFriendEnterHomeOption(FriendEnterHomeOption.FRIEND_ENTER_HOME_OPTION_REFUSE.getValue())
            /// TODO: .setIsChatNoDisturb(false)
            ///  todo: remark_name
            .setIsGameSource(true)
            .setIsMpModeAvailable(false)
            .setLastActiveTime((int)(myAccount.getLastLoginDate() / 1000))
            .setLevel(myAccount.getPlayerLevel())
            .setNameCardId(myAccount.getNameCardId())
            .setNickname(myAccount.getUsername())
            .setOnlineState(myPlayer != null ? 1 : 0)
            .setPlatformType(3)
            .setProfilePicture(ProfilePicture.newBuilder().setAvatarId(myAccount.getProfileAvatarImageId()).setCostumeId(myAccount.getProfileAvatarCostumeImageId()).build())
            .setSignature(myAccount.getProfileSignature())
            .setUid(myAccount.getId().intValue())
            .setWorldLevel(myAccount.getWorldLevel())
            .build();
    }

    /**
     * Gets the player's social information in the game.
     * @param targetUid The target uid.
     * @param isFriend Is the current player friend with the target.
     * @param isIgnored Is the current player ignored with the target.
     * @return A SocialDetail protobuf object.
     */
    public SocialDetail getPlayerSocialInfo(long targetUid, boolean isFriend, boolean isIgnored) {
        var myPlayer = this.players.get(targetUid);
        var myAccount = (myPlayer != null ? myPlayer.getAccount() : DBUtils.findAccountById(targetUid));
        if(myAccount == null) {
            return null;
        }

        List<SocialShowAvatarInfo> socialShowAvatarInfoList = new ArrayList<>();
        for(int avatarId : myAccount.getUnlockedAvatarsProfileShown()) {
            var avatarEntry = myAccount.getUnlockedAvatars().get(avatarId);
            socialShowAvatarInfoList.add(SocialShowAvatarInfo.newBuilder().setAvatarId(avatarId).setLevel(avatarEntry.getLevel()).setCostumeId(avatarEntry.getCostumeId()).build());
        }

        return SocialDetail.newBuilder()
            .addAllShowAvatarInfoList(socialShowAvatarInfoList)
            .addAllShowNameCardIdList(myAccount.getUnlockedNameCardsProfileShown())
            .setBirthday(myAccount.getPlayerBirthday().toProto())
            .setIsFriend(isFriend)
            .setIsInBlacklist(isIgnored)
            .setIsShowAvatar(myAccount.isShowProfileAvatars())
            .setLevel(myAccount.getPlayerLevel())
            .setNameCardId(myAccount.getNameCardId())
            .setNickname(myAccount.getUsername())
            .setOnlineState(myPlayer != null ? 1 : 0)
            .setProfilePicture(ProfilePicture.newBuilder().setAvatarId(myAccount.getProfileAvatarImageId()).setCostumeId(myAccount.getProfileAvatarCostumeImageId()).build())
            .setSignature(myAccount.getProfileSignature())
            .setUid(myAccount.getId().intValue())
            .setWorldLevel(myAccount.getWorldLevel())
                ///  TODO: is_mp_mode_available
                ///  TODO: is_chat_no_disturb
                ///  TODO: remark_name
                ///  TODO: finish_achievement_num
                ///  TODO: tower_floor_index, tower_level_index
                ///  TODO: friend_enter_home_option
            .build();
    }







    /**
     * Shutdowns the server.
     */
    public void shutdownServer() {

        ///  TODO: FINISH
        for(var player : this.players.values()) {
            player.closeConnection();
        }
    }

    public void sendPlayerSanction(long playerUid, int durationHours, String moderator) {

    }
}