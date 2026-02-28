package org.genshinimpact.gameserver.connection;

// Imports
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.gameserver.connection.kcp.KcpSession;
import org.genshinimpact.gameserver.connection.kcp.KcpTunnel;
import org.genshinimpact.gameserver.game.Player;
import org.genshinimpact.gameserver.game.Server;
import org.genshinimpact.gameserver.packets.BadPacketException;

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
        ///  TODO: IMPLEMENT
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
        ///  TODO: IMPLEMENT
        return -1;
    }
}