package org.emilieps.data;

public final class PacketIdentifiers {
    public static final int PACKET_HEAD =  0x4567;
    public static final int PACKET_MAGIC = -0x7655;

    public static class Receive {
        public static final int PingReq = 5983;
        public static final int GetPlayerTokenReq = 6013;
        public static final int PlayerLoginReq = 2422;
        public static final int GetPlayerFriendListReq = 21607;
        public static final int GetPlayerBlacklistReq = 6441;

        public static final int GetShopReq = 4619;
    }

    public static class Send {
        public static final int PingRsp = 22595;
        public static final int GetPlayerTokenRsp = 24174;
        public static final int ServerDisconnectClientNotify = 1387;
        public static final int PlayerLoginRsp = 27771;
    }
}