package org.emilieps.game.connection;

// Imports
import java.lang.reflect.Field;
import org.bytearray.ByteArray;
import org.emilieps.Application;
import org.emilieps.game.Server;
import org.emilieps.game.packets.PacketIdentifiers;
import org.emilieps.libraries.EncryptionManager;

public final class ClientHandler implements KcpChannelHandler {
    private static final int PACKET_HEAD =  0x4567;
    private static final int PACKET_MAGIC = -0x7655;
    private final Server server;

    public ClientHandler(Server server) {
        this.server = server;
    }

    @Override
    public void onConnected(ClientSession session) {
        Application.getLogger().info(Application.getTranslationManager().get("console", "newgameconnection", session.getIpAddress()));
    }

    @Override
    public void onClosed(ClientSession session) {
        Application.getLogger().info(Application.getTranslationManager().get("console", "closedgameconnection", session.getIpAddress()));
        session.closeConnection();
    }

    @Override
    public void onMessageReceived(ClientSession session, byte[] bytes) {
        ByteArray packet = new ByteArray(EncryptionManager.performXor(bytes, this.useSecretKey(session) ? EncryptionManager.getSecretKey() : EncryptionManager.getDispatchKey()));
        if(packet.readShort() != PACKET_HEAD) {
            Application.getLogger().info(Application.getTranslationManager().get("console", "badpacketfound", session.getIpAddress()));
            session.closeConnection();
        }

        int opcode = packet.readShort();
        int headerLength = packet.readShort();
        int payloadLength = packet.readInt();
        byte[] header = packet.readBytes(headerLength);
        byte[] payload = packet.readBytes(payloadLength);
        if(packet.readShort() != PACKET_MAGIC) {
            Application.getLogger().info(Application.getTranslationManager().get("console", "badpacketfound", session.getIpAddress()));
            session.closeConnection();
        }

        var handler = this.server.getPacketHandler().getHandlers().get(opcode);
        if(handler != null) {
            handler.handle(session, header, payload);
        } else {
            Application.getLogger().warn(Application.getTranslationManager().get("console", "packetnotfound", opcode, this.getPacketName(opcode)));
        }
    }

    @Override
    public void exceptionCaught(Throwable ex) {
        Application.getLogger().error(Application.getTranslationManager().get("console", "packeterror", ex));
    }

    /**
     * Checks if the session is going to use the secret key (packet key). A session is using secret key after sending GetPlayerTokenReq.
     * @param session The given session.
     * @return True if its going to use secret key or false.
     */
    private Boolean useSecretKey(ClientSession session) {
        return (session.getSessionState() != SessionState.INACTIVE) && (session.getSessionState() != SessionState.WAITING_FOR_TOKEN);
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
        return "UnknownPacket";
    }
}