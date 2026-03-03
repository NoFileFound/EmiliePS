package org.genshinimpact.gameserver.connection;

// Imports
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.net.InetSocketAddress;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.connection.kcp.KcpTunnel;
import org.genshinimpact.gameserver.packets.BadPacketException;
import org.kcp.KcpListener;
import org.kcp.Ukcp;

@lombok.RequiredArgsConstructor
public final class ClientHandler implements KcpListener {
    private final Server server;

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
        this.server.addSession(ukcp, session);
    }

    @Override
    public void handleReceive(ByteBuf byteBuf, Ukcp ukcp) {
        try {
            this.server.sessionReceiveData(ukcp, byteBuf);
        } catch(BadPacketException ex) {
            AppBootstrap.getLogger().error("[Game] Bad packet -> {}", ukcp.user().getRemoteAddress().toString(), ex);
        }
    }

    @Override
    public void handleException(Throwable ex, Ukcp ukcp) {
        AppBootstrap.getLogger().trace("[Game] {}", ukcp.user().getRemoteAddress().toString(), ex);
    }

    @Override
    public void handleClose(Ukcp ukcp) {
        this.server.removeSession(ukcp);
    }
}