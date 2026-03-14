package org.genshinimpact.gameserver.packets;

public final class PacketIdentifiers {
    public static final int PACKET_HEAD = 0x4567;
    public static final int PACKET_MAGIC = -0x7655;

    public static final class Receive {
        public static final int PingReq = 7;
        public static final int SetPlayerBornDataReq = 105;
        public static final int PlayerLoginReq = 112;
        public static final int PlayerSetPauseReq = 124;
        public static final int SetOpenStateReq = 165;
        public static final int GetPlayerTokenReq = 172;
        public static final int GetShopReq = 772;
        public static final int GetPlayerBlacklistReq = 4049;
        public static final int GetChatEmojiCollectionReq = 4068;
        public static final int GetPlayerFriendListReq = 4072;
    }

    public static final class Send {
        public static final int PingRsp = 21;
        public static final int PlayerLoginRsp = 135;
        public static final int DoSetPlayerBornDataNotify = 147;
        public static final int PlayerSetPauseRsp = 156;
        public static final int SetPlayerBornDataRsp = 182;
        public static final int ServerDisconnectClientNotify = 184;
        public static final int PlayerDataNotify = 190;
        public static final int GetPlayerTokenRsp = 198;
        public static final int PlayerEnterSceneNotify = 272;
    }
}