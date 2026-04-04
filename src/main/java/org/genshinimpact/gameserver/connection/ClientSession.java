package org.genshinimpact.gameserver.connection;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.Signature;
import java.time.Instant;
import javax.crypto.Cipher;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.database.DBUtils;
import org.genshinimpact.database.collections.Account;
import org.genshinimpact.database.collections.Guest;
import org.genshinimpact.gameserver.connection.kcp.KcpSession;
import org.genshinimpact.gameserver.connection.kcp.KcpTunnel;
import org.genshinimpact.gameserver.enums.Retcode;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.packets.BadPacketException;
import org.genshinimpact.gameserver.packets.PacketIdentifiers;
import org.genshinimpact.gameserver.packets.SendPacket;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.enums.ClientType;
import org.genshinimpact.utils.CryptoUtils;

// Packets
import org.genshinimpact.gameserver.packets.send.SendPingRsp;
import org.genshinimpact.gameserver.packets.send.SendServerDisconnectClientNotify;
import org.genshinimpact.gameserver.packets.send.player.SendGetPlayerTokenRsp;

// Protocol buffers
import org.generated.protobuf.GetPlayerTokenReqOuterClass.GetPlayerTokenReq;
import org.generated.protobuf.PacketHeadOuterClass.PacketHead;
import org.generated.protobuf.PingReqOuterClass.PingReq;

@Getter
public final class ClientSession implements KcpSession {
    @Setter private SessionState state;
    private final Server server;
    private KcpTunnel tunnel;
    private Player player;
    private long clientTime;
    private long lastPingTime;
    private int clientSequence = 1;

    /**
     * Creates a new instance of client session.
     */
    public ClientSession(Server server) {
        this.server = server;
        this.state = SessionState.CLOSED;
    }

    /**
     * Handles when client makes a session.
     */
    @Override
    public void onConnect(KcpTunnel tunnel) {
        this.tunnel = tunnel;
        this.state = SessionState.WAITING_FOR_TOKEN;
    }

    /**
     * Handles when the session receives data.
     * @param data The packet data.
     */
    @Override
    public void onReceive(ByteBuf data) throws BadPacketException, InvalidProtocolBufferException {
        data = Unpooled.wrappedBuffer(CryptoUtils.getXor(ByteBufUtil.getBytes(data), this.state != SessionState.WAITING_FOR_TOKEN ? CryptoUtils.getClientSecretKey() : new byte[] {0}));
        if(data.readableBytes() < 12)
            return;

        int topMagic = data.readShort();
        if(topMagic != PacketIdentifiers.PACKET_HEAD) {
            throw new BadPacketException("Expected a top magic value of " + PacketIdentifiers.PACKET_HEAD + ", got " + topMagic);
        }

        int packetId = data.readShort();
        int packetHeaderLen = data.readUnsignedShort();
        int packetDataLen = data.readInt();
        if(data.readableBytes() < packetHeaderLen + packetDataLen + 2) {
            return;
        }

        byte[] packetHeader = new byte[packetHeaderLen];
        data.readBytes(packetHeader);
        byte[] packetData = new byte[packetDataLen];
        data.readBytes(packetData);
        int bottomMagic = data.readShort();
        if(bottomMagic != PacketIdentifiers.PACKET_MAGIC) {
            throw new BadPacketException("Expected a bottom magic value of " + PacketIdentifiers.PACKET_MAGIC + ", got " + bottomMagic);
        }

        var packetName = this.server.getPacketManager().getPacketName(packetId);
        var ip = this.tunnel.getAddress().getAddress().getHostAddress();
        if(packetId == PacketIdentifiers.Receive.GetPlayerTokenReq) {
            AppBootstrap.getLogger().info("[Game] The IP Address {} received a packet -> 172 [GetPlayerTokenReq]", ip);
            this.handleConnectionHandshake(packetData);
        } else if(packetId == PacketIdentifiers.Receive.PingReq) {
            AppBootstrap.getLogger().info("[Game] The IP Address {} received a packet -> 7 [PingReq]", ip);
            this.handlePingRequest(packetData);
        } else {
            var handler = this.server.getPacketManager().getHandlers().get(packetId);
            if(handler != null) {
                AppBootstrap.getLogger().info("[Game] The IP Address {} received a packet -> {} [{}]", ip, packetId, packetName);
                try {
                    handler.handle(this.player, packetHeader, packetData);
                } catch(Exception ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                AppBootstrap.getLogger().info("[Game] The IP Address {} found unknown packet -> {}", ip, packetName);
            }
        }
    }

    /**
     * Handles when the session closes connection.
     */
    @Override
    public void onClose() {
        this.sendPacket(new SendServerDisconnectClientNotify());
        this.state = SessionState.CLOSED;
        this.tunnel = null;
    }

    /**
     * Sends a packet from the current session.
     * @param packet The packet to send.
     * @param sequenceId The client's sequence id.
     */
    public void sendPacket(SendPacket packet, int sequenceId) {
        byte[] packetHeader = PacketHead.newBuilder()
                .setSentMs(System.currentTimeMillis())
                .setClientSequenceId(sequenceId)
                .setEnetChannelId(0)
                .setEnetIsReliable(1) // true
                .build()
                .toByteArray();

        ByteArrayOutputStream data = new ByteArrayOutputStream(2 + 2 + 2 + 4 + packetHeader.length + packet.getPacket().length + 2);
        data.write((byte)((PacketIdentifiers.PACKET_HEAD >>> 8) & 0xFF));
        data.write((byte)(PacketIdentifiers.PACKET_HEAD & 0xFF));
        data.write((byte)((packet.getCode() >>> 8) & 0xFF));
        data.write((byte)(packet.getCode() & 0xFF));
        data.write((byte)((packetHeader.length >>> 8) & 0xFF));
        data.write((byte)(packetHeader.length & 0xFF));
        data.write((byte)((packet.getPacket().length >>> 24) & 0xFF));
        data.write((byte)((packet.getPacket().length >>> 16) & 0xFF));
        data.write((byte)((packet.getPacket().length >>> 8) & 0xFF));
        data.write((byte)(packet.getPacket().length & 0xFF));
        data.writeBytes(packetHeader);
        data.writeBytes(packet.getPacket());
        data.write((byte)((PacketIdentifiers.PACKET_MAGIC >>> 8) & 0xFF));
        data.write((byte)(PacketIdentifiers.PACKET_MAGIC & 0xFF));
        if(this.state == SessionState.WAITING_FOR_TOKEN) {
            if(packet.getCode() != PacketIdentifiers.Send.PingRsp && packet.getCode() != PacketIdentifiers.Send.GetPlayerTokenRsp) {
                this.tunnel.close();
                return;
            }

            this.tunnel.writeData(data.toByteArray());
            if(packet.getCode() == PacketIdentifiers.Send.GetPlayerTokenRsp) {
                this.setState(SessionState.WAITING_FOR_LOGIN);
            }
        } else {
            this.tunnel.writeData(CryptoUtils.getXor(data.toByteArray(), CryptoUtils.getClientSecretKey()));
        }

        AppBootstrap.getLogger().info("[Game] The IP Address {} send an packet -> {} [{}]", this.tunnel.getAddress().getAddress().getHostAddress(), packet.getCode(), this.server.getPacketManager().getPacketName(packet.getCode()));
    }

    /**
     * Sends a packet from the current session.
     * @param packet The packet to send.
     */
    public void sendPacket(SendPacket packet) {
        this.sendPacket(packet, ++this.clientSequence);
    }

    /**
     * Handles the GetPlayerTokenReq packet (Connection Handshake).
     * @param data The data from the protobuf.
     */
    private void handleConnectionHandshake(byte[] data) throws InvalidProtocolBufferException {
        var req = GetPlayerTokenReq.parseFrom(data);
        try {
            boolean isGuest = req.getIsGuest();
            ClientType myPlatform = ClientType.fromValue(String.valueOf(req.getPlatformType()));
            if(myPlatform == ClientType.PLATFORM_UNKNOWN) {
                this.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_UNKNOWN_PLATFORM, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
                return;
            }

            if(AppBootstrap.getMainConfig().badIPS.contains(this.getTunnel().getAddress().getAddress().getHostAddress())) {
                this.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_BLACK_LOGIN_IP, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
                return;
            }

            var myIdentity = isGuest ? DBUtils.findGuestById(Long.parseLong(req.getAccountUid())) : DBUtils.findAccountById(Long.parseLong(req.getAccountUid()));
            if(myIdentity == null) {
                this.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_ACCOUNT_NOT_EXIST, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
                return;
            }

            if(!myIdentity.getComboToken().equals(req.getAccountToken())) {
                this.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_TOKEN_ERROR, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
                return;
            }

            if(myIdentity instanceof Account account) {
                if(account.getRequireRealPerson() || account.getRequireDeviceGrant() || account.getRequireSafeMobile() || account.getEmailBindTicket() != null) {
                    this.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_FORBIDDEN, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
                    return;
                }

                var mySanctions = DBUtils.findSanctionListByAccountId(myIdentity.getId());
                if(!mySanctions.isEmpty()) {
                    for(var sanction : mySanctions) {
                        long hours = (sanction.getExpirationDate() - (System.currentTimeMillis() / 1000)) / 3600;
                        if(hours <= 0) {
                            sanction.setState("Expired");
                            sanction.save();
                        } else {
                            this.sendPacket(new SendGetPlayerTokenRsp(req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getPlatformType(), 3, req.getChannelId(), req.getSubChannelId(), String.valueOf(sanction.getSanctionType()), sanction.getExpirationDate()));
                            return;
                        }
                    }
                }

                if(account.getRequireHeartbeat()) {
                    Instant now = Instant.now();
                    Instant start = SpringBootApp.getHeartbeatService().getHeartBeatCache().get(this.tunnel.getAddress().getAddress().getHostAddress(), k -> now);
                    long elapsed = now.getEpochSecond() - start.getEpochSecond();
                    if(elapsed >= 5400) {
                        this.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_ANTI_ADDICT, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
                        return;
                    }
                }
            }

            if(this.server.getPlayers().size() + 1 > AppBootstrap.getMainConfig().maximumPlayers) {
                this.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_MAX_PLAYER, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
                return;
            }

            var myPlayer = this.server.getPlayers().get(myIdentity.getId());
            if(myPlayer != null) {
                myPlayer.closeConnection();
                this.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_ANOTHER_LOGIN, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
                return;
            }

            if(myIdentity instanceof Account account) {
                myPlayer = new Player(account, this);
            } else {
                myPlayer = new Player((Guest)myIdentity, this);
            }

            this.player = myPlayer;
            if(req.getKeyId() > 0) {
                var encryptSeed = CryptoUtils.getClientSecretKeySeed();
                try {
                    var cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    cipher.init(Cipher.DECRYPT_MODE, CryptoUtils.getDispatchSignatureKey());
                    var clientSeed = ByteBuffer.wrap(cipher.doFinal(CryptoUtils.decodeBase64(req.getClientRandKey()))).getLong();
                    var seedBytes = ByteBuffer.wrap(new byte[8]).putLong(encryptSeed ^ clientSeed).array();
                    cipher.init(Cipher.ENCRYPT_MODE, CryptoUtils.getDispatchEncryptionKeys().get(req.getKeyId()));
                    var privateSignature = Signature.getInstance("SHA256withRSA");
                    privateSignature.initSign(CryptoUtils.getDispatchSignatureKey());
                    privateSignature.update(seedBytes);
                    this.sendPacket(new SendGetPlayerTokenRsp(req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), 3, req.getChannelId(), req.getSubChannelId(), CryptoUtils.encodeBase64(cipher.doFinal(seedBytes)), CryptoUtils.encodeBase64(privateSignature.sign()), req.getBirthday(), req.getCountryCode(), this.tunnel.getAddress().getAddress().getHostAddress(), this.player.getAvatarStorage().getTotalAvatars()));
                } catch(Exception ignored) {
                    this.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_ACCOUNT_VERIFY_ERROR, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
                }
            }
        } catch(Exception ignored) {
            this.sendPacket(new SendGetPlayerTokenRsp(Retcode.RET_LOGIN_INIT_FAIL, req.getAccountUid(), req.getPsnId(), req.getAccountToken(), req.getIsGuest(), req.getPlatformType(), req.getChannelId(), req.getSubChannelId()));
        }
    }

    /**
     * Handles the PingReq packet.
     * @param data The data from the protobuf.
     */
    private void handlePingRequest(byte[] data) throws InvalidProtocolBufferException {
        var req = PingReq.parseFrom(data);
        this.clientTime = req.getClientTime();
        this.lastPingTime = System.currentTimeMillis();
        this.sendPacket(new SendPingRsp(req.getClientTime(), req.getSeq()), req.getSeq());
    }
}