package org.genshinimpact.gameserver.packets;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.connection.ClientSession;

public interface PacketHandler {
    /**
     * Handle an incoming packet
     * @param packet The packet being sent
     * @param session The session that sent the packet
     */
    void handle(InboundPacket packet, ClientSession session) throws InvalidProtocolBufferException;

    /**
     * Gets the packet id.
     * @return The packet id.
     */
    int getCode();
}