package org.genshinimpact.gameserver.connection;

// Imports
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import java.io.ByteArrayOutputStream;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.gameserver.connection.kcp.KcpSession;
import org.genshinimpact.gameserver.connection.kcp.KcpTunnel;
import org.genshinimpact.gameserver.game.player.Player;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.packets.BadPacketException;
import org.genshinimpact.gameserver.packets.PacketIdentifiers;
import org.genshinimpact.gameserver.packets.SendPacket;
import org.genshinimpact.gameserver.packets.send.player.SendServerDisconnectClientNotify;
import org.genshinimpact.utils.CryptoUtils;

// Protocol buffers
import org.generated.protobuf.PacketHeadOuterClass.PacketHead;

@Getter
public final class ClientSession implements KcpSession {
    @Getter @Setter private Player player;
    @Setter private SessionState state;
    private final Server server;
    private int clientSequence = 1;
    private KcpTunnel tunnel;

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
    public void onReceive(ByteBuf data) throws BadPacketException {
        data = Unpooled.wrappedBuffer(CryptoUtils.getXor(ByteBufUtil.getBytes(data), this.state != SessionState.WAITING_FOR_TOKEN ? CryptoUtils.getClientSecretKey() : new byte[] {0}));
        int topMagic = data.readShort();
        if(topMagic != PacketIdentifiers.PACKET_HEAD) {
            throw new BadPacketException("Expected a top magic value of " + PacketIdentifiers.PACKET_HEAD + ", got " + topMagic);
        }

        int packetId = data.readShort();
        int packetHeaderLen = data.readShort(), packetDataLen = data.readInt();
        byte[] packetHeader = new byte[packetHeaderLen];
        data.readBytes(packetHeader);
        byte[] packetData = new byte[packetDataLen];
        data.readBytes(packetData);
        int bottomMagic = data.readShort();
        if(bottomMagic != PacketIdentifiers.PACKET_MAGIC) {
            throw new BadPacketException("Expected a bottom magic value of " + PacketIdentifiers.PACKET_MAGIC + ", got " + bottomMagic);
        }

        var handler = this.server.getPacketManager().getHandlers().get(packetId);
        if(handler != null) {
            AppBootstrap.getLogger().info("[Game] The IP Address {} received an packet -> {} [{}]", this.tunnel.getAddress().getAddress().getHostAddress(), packetId, this.server.getPacketManager().getPacketName(packetId));
            try {
                handler.handle(this, packetHeader, packetData);
            } catch(Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            AppBootstrap.getLogger().info("[Game] The IP Address {} found unknown packet packet -> {}", this.tunnel.getAddress().getAddress().getHostAddress(), this.server.getPacketManager().getPacketName(packetId));
        }
    }

    /**
     * Handles when the session closes connection.
     */
    @Override
    public void onClose() {
        if(this.player != null) {
            this.player.closeConnection();
        }

        this.state = SessionState.CLOSED;
        this.sendPacket(new SendServerDisconnectClientNotify());
        this.tunnel = null;
    }

    /**
     * Gets the session's account id.
     */
    @Override
    public long getUid() {
        return this.player.getPlayerIdentity().getId();
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
}