package org.emilieps.game;

// Imports
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.net.InetSocketAddress;
import lombok.RequiredArgsConstructor;
import org.emilieps.Application;
import org.emilieps.game.connection.ClientSession;
import org.emilieps.game.connection.KcpTunnel;
import org.emilieps.game.packets.base.BadPacketException;
import org.kcp.KcpListener;
import org.kcp.Ukcp;

@RequiredArgsConstructor
public final class GameServerListener implements KcpListener {
    private final GameServer server;

    @Override
    public void onConnected(Ukcp ukcp) {
        KcpTunnel connection = new KcpTunnel() {
            @Override
            public InetSocketAddress getAddress() {
                return ukcp.user().getRemoteAddress();
            }

            @Override
            public void writeData(byte[] bytes) {
                ByteBuf buf = Unpooled.wrappedBuffer(bytes);
                ukcp.write(buf);
                buf.release();
            }

            @Override
            public void close() {
                ukcp.close();
            }
        };

        ClientSession session = new ClientSession(this.server);
        session.onConnect(connection);
        this.server.getSessions().put(ukcp, session);
        if(Application.getApplicationConfig().is_debug_packets) {
            Application.getLogger().info(Application.getTranslations().get("console", "newgameconnection", ukcp.user().getRemoteAddress().getAddress().getHostAddress()));
        }
    }

    @Override
    public void handleReceive(ByteBuf byteBuf, Ukcp ukcp) {
        try {
            if (this.server.getSessions().containsKey(ukcp)) {
                this.server.getSessions().get(ukcp).onReceive(byteBuf);
            }
        } catch (BadPacketException ex) {
            Application.getLogger().info(Application.getTranslations().get("console", "badpacketfound", ukcp.user().getRemoteAddress().toString()));
            ex.printStackTrace();
        }
    }

    @Override
    public void handleException(Throwable ex, Ukcp ukcp) {
        Application.getLogger().error(Application.getTranslations().get("console", "packeterror", ex.getCause()), ex);
    }

    @Override
    public void handleClose(Ukcp ukcp) {
        if (this.server.getSessions().containsKey(ukcp)) {
            this.server.getSessions().get(ukcp).onClose();
            this.server.getSessions().remove(ukcp);
            if(Application.getApplicationConfig().is_debug_packets) {
                Application.getLogger().info(Application.getTranslations().get("console", "closedgameconnection", ukcp.user().getRemoteAddress().getAddress().getHostAddress()));
            }
        }
    }
}