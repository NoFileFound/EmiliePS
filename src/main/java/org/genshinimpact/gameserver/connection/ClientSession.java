package org.genshinimpact.gameserver.connection;

// Imports
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.gameserver.connection.kcp.KcpSession;
import org.genshinimpact.gameserver.connection.kcp.KcpTunnel;
import org.genshinimpact.gameserver.game.Player;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.packets.BadPacketException;
import org.genshinimpact.gameserver.packets.InboundPacket;
import org.genshinimpact.gameserver.packets.OutboundPacket;
import org.genshinimpact.gameserver.packets.send.player.SendGetPlayerTokenRsp;
import org.genshinimpact.utils.CryptoUtils;

@Getter
public final class ClientSession implements KcpSession {
    @Getter @Setter private Player player;
    @Setter private SessionState state;
    private final Server server;
    private int clientSequence = 10;
    private KcpTunnel tunnel;

    public ClientSession(Server server) {
        this.server = server;
        this.state = SessionState.CLOSED;
    }

    @Override
    public void onConnect(KcpTunnel tunnel) {
        this.tunnel = tunnel;
        this.state = SessionState.WAITING_FOR_TOKEN;
    }

    @Override
    public void onReceive(ByteBuf data) throws BadPacketException {
        byte[] encryptionKey = this.state != SessionState.WAITING_FOR_TOKEN ? CryptoUtils.getClientSecretKey() : new byte[] {0};
        InboundPacket packet = new InboundPacket(Unpooled.wrappedBuffer(CryptoUtils.getXor(ByteBufUtil.getBytes(data), encryptionKey)), this);
        var handler = this.server.getPacketManager().getHandlers().get(packet.getId());
        if(handler != null) {
            try {
                handler.handle(packet, this);
                AppBootstrap.getLogger().info("[Game] The IP Address {} received an packet -> {} [{}]", this.tunnel.getAddress().toString(), packet.getId(), "");
            } catch(Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            AppBootstrap.getLogger().info("[Game] The IP Address {} found unknown packet packet -> {}", this.tunnel.getAddress().toString(), packet.getId());
        }
    }

    @Override
    public void onClose() {
        ///  TODO: ServerDisconnectClientNotify
        this.state = SessionState.CLOSED;
    }

    @Override
    public int getNextClientSequence() {
        return ++this.clientSequence;
    }

    @Override
    public long getUid() {
        return this.player.getAccount().getId();
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
        AppBootstrap.getLogger().info("[Game] The IP Address {} send an packet -> {} [{}]", this.tunnel.getAddress().toString(), packet.getId(), "");
    }
}

/// TODO: GET PACKET NAME FROM ID.