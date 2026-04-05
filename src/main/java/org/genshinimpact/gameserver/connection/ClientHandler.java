package org.genshinimpact.gameserver.connection;

// Imports
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.net.InetSocketAddress;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.connection.kcp.KcpTunnel;
import org.kcp.KcpListener;
import org.kcp.Ukcp;

@lombok.RequiredArgsConstructor
public final class ClientHandler implements KcpListener {
    private final Server server;

    /**
     * Handles when the client connects to the server.
     * @param ukcp The User's KCP.
     */
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

    /**
     * Handles when the client receives data.
     * @param ukcp The User's KCP.
     */
    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void handleReceive(ByteBuf byteBuf, Ukcp ukcp) {
        try {
            this.server.sessionReceiveData(ukcp, byteBuf);
        } catch(Exception ex) {
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(byteBuf.readerIndex(), bytes);
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for(byte b : bytes) {
                hex.append(String.format("%02X", b));
            }

            AppBootstrap.getLogger().error("[Game] Bad packet -> {}", ukcp.user().getRemoteAddress().toString(), ex);
            AppBootstrap.getLogger().error("{}", hex);
            ex.printStackTrace();
        }
    }

    /**
     * Handles when the server throws exception.
     * @param ukcp The User's KCP.
     * @param ex The throwable exception.
     */
    @Override
    public void handleException(Throwable ex, Ukcp ukcp) {
        AppBootstrap.getLogger().error("[Game] KCP Error -> {}", ukcp.user().getRemoteAddress().getAddress().getHostAddress(), ex);
    }

    /**
     * Handles when the client closes the connection to the server.
     * @param ukcp The User's KCP.
     */
    @Override
    public void handleClose(Ukcp ukcp) {
        this.server.removeSession(ukcp);
    }
}