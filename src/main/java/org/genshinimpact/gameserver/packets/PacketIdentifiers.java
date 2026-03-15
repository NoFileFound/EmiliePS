package org.genshinimpact.gameserver.packets;

public final class PacketIdentifiers {
    public static final int PACKET_HEAD = 0x4567;
    public static final int PACKET_MAGIC = -0x7655;

    public static final class Receive {
        public static final int UnionCmdNotify = 5;
        public static final int PingReq = 7;
        public static final int SetPlayerBornDataReq = 105;
        public static final int PlayerLoginReq = 112;
        public static final int PlayerSetPauseReq = 124;
        public static final int SetOpenStateReq = 165;
        public static final int GetPlayerTokenReq = 172;
        public static final int EnterSceneReadyReq = 208;
        public static final int SceneInitFinishReq = 235;
        public static final int EnterWorldAreaReq = 250;
        public static final int GetSceneAreaReq = 265;
        public static final int EnterSceneDoneReq = 277;
        public static final int GetScenePointReq = 297;
        public static final int GetAllActivatedBargainDataReq = 463;
        public static final int GetShopReq = 772;
        public static final int ClientAbilityInitFinishNotify = 1135;
        public static final int ClientAbilityChangeNotify = 1175;
        public static final int GetInvestigationMonsterReq = 1901;
        public static final int PathfindingEnterSceneReq = 2307;
        public static final int TowerAllDataReq = 2490;
        public static final int GetMapAreaReq = 3108;
        public static final int EntityConfigHashNotifyReq = 3189;
        public static final int PostEnterSceneReq = 3312;
        public static final int GetPlayerAskFriendListReq = 4018;
        public static final int GetPlayerBlacklistReq = 4049;
        public static final int GetChatEmojiCollectionReq = 4068;
        public static final int GetPlayerFriendListReq = 4072;
        public static final int GetPlayerSocialDetailReq = 4073;
        public static final int QueryCodexMonsterBeKilledNumReq = 4203;
        public static final int GetWidgetSlotReq = 4253;
        public static final int GetHomeExchangeWoodInfoReq = 4473;
        public static final int FurnitureMakeReq = 4477;
        public static final int GetPlayerHomeCompInfoReq = 4597;
        public static final int GetFurnitureCurModuleArrangeCountReq = 4711;
        public static final int PullRecentChatReq = 5040;
        public static final int ReunionBriefInfoReq = 5076;
        public static final int GetAllSceneGalleryInfoReq = 5503;
        public static final int GetRegionSearchReq = 5602;
        public static final int ToTheMoonEnterSceneReq = 6135;
    }

    public static final class Send {
        public static final int PingRsp = 21;
        public static final int PlayerGameTimeNotify = 131;
        public static final int PlayerLoginRsp = 135;
        public static final int DoSetPlayerBornDataNotify = 147;
        public static final int PlayerSetPauseRsp = 156;
        public static final int SetPlayerBornDataRsp = 182;
        public static final int ServerDisconnectClientNotify = 184;
        public static final int PlayerDataNotify = 190;
        public static final int GetPlayerTokenRsp = 198;
        public static final int SceneInitFinishRsp = 207;
        public static final int EnterSceneReadyRsp = 209;
        public static final int PlayerEnterSceneInfoNotify = 214;
        public static final int EnterSceneDoneRsp = 237;
        public static final int SceneTimeNotify = 245;
        public static final int EnterScenePeerNotify = 252;
        public static final int ScenePlayerInfoNotify = 267;
        public static final int PlayerEnterSceneNotify = 272;
        public static final int SyncTeamEntityNotify = 317;
        public static final int StoreWeightLimitNotify = 698;
        public static final int AvatarDataNotify = 1633;
        public static final int SceneTeamUpdateNotify = 1775;
        public static final int PostEnterSceneRep = 3184;
        public static final int HostPlayerNotify = 3203;
        public static final int WorldDataNotify = 3308;
        public static final int SyncScenePlayTeamEntityNotify = 3333;
    }
}