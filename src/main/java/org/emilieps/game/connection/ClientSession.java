package org.emilieps.game.connection;

// Imports
import java.lang.reflect.Field;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.emilieps.Application;
import org.emilieps.data.PacketIdentifiers;
import org.emilieps.game.GameServer;
import org.emilieps.game.packets.base.BadPacketException;
import org.emilieps.game.packets.base.InboundPacket;
import org.emilieps.game.packets.base.OutboundPacket;
import org.emilieps.game.player.Player;

// Libraries
import org.emilieps.library.EncryptionLib;

@Getter
@RequiredArgsConstructor
public final class ClientSession implements KcpSession {
    @Setter private SessionState state = SessionState.CLOSED;
    @Setter private Player player = null;
    @Setter private Integer lastPingTime;
    @Setter private Integer lastFPS;
    private KcpTunnel tunnel;
    private int clientSequence = 10;
    private final GameServer server;

    @Override
    public void onConnect(KcpTunnel tunnel) {
        this.tunnel = tunnel;
        this.state = SessionState.WAITING_FOR_TOKEN;
    }

    @Override
    public void onReceive(ByteBuf data) throws BadPacketException {
        InboundPacket packet = new InboundPacket(Unpooled.wrappedBuffer(EncryptionLib.performXor(ByteBufUtil.getBytes(data), (this.getState() != SessionState.CLOSED && this.getState() != SessionState.WAITING_FOR_TOKEN) ? EncryptionLib.getSecretKey() : EncryptionLib.getDispatchKey())), this);
        var handler = this.server.getPacketManager().getHandlers().get(packet.getId());
        if(handler != null) {
            try {
                handler.handle(packet, this);
                if(Application.getApplicationConfig().is_debug_packets) {
                    Application.getLogger().info(Application.getTranslations().get("console", "packetreceive",  this.tunnel.getAddress().toString(), packet.getId(), this.getPacketName(packet.getId())));
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            Application.getLogger().warn(Application.getTranslations().get("console", "packetnotfound", packet.getId(), this.getPacketName(packet.getId())));
        }
    }

    @Override
    public void onClose() {
        if (this.player != null) {
            this.player.logout();
        }

        this.setState(SessionState.CLOSED);
        this.sendPacket(new OutboundPacket(PacketIdentifiers.Send.ServerDisconnectClientNotify));
    }

    /**
     * @return The next client sequence to use
     */
    @Override
    public int getNextClientSequence() {
        return ++this.clientSequence;
    }

    /**
     * @return The UID of the associated player, or 0 if none exists
     */
    @Override
    public int getUid() {
        return this.player != null ? this.player.getId() : -1;
    }

    /**
     * Sends a packet from the current session.
     * @param packet The packet to send.
     */
    public void sendPacket(OutboundPacket packet) {
        if(packet.isShouldBuildHeader()) {
            packet.buildHeader(this.getNextClientSequence());
        }

        this.tunnel.writeData(packet.build());
        if(Application.getApplicationConfig().is_debug_packets) {
            Application.getLogger().info(Application.getTranslations().get("console", "packetsent",  this.tunnel.getAddress().toString(), packet.getId(), this.getPacketName(packet.getId())));
        }
    }

    /**
     * Gets the packet name from the packet identifiers.
     * @param id The packet opcode.
     * @return Gets the packet name from the packet identifiers if exist or else UnknownPacket.
     */
    private String getPacketName(int id) {
        for (Field field : PacketIdentifiers.Receive.class.getDeclaredFields()) {
            try {
                if (field.getInt(null) == id) {
                    return field.getName();
                }
            } catch (IllegalAccessException ignored) {}
        }

        for (Field field : PacketIdentifiers.Send.class.getDeclaredFields()) {
            try {
                if (field.getInt(null) == id) {
                    return field.getName();
                }
            } catch (IllegalAccessException ignored) {}
        }
        return "UnknownPacket";
    }
}