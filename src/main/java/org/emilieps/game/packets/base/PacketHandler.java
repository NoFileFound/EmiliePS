package org.emilieps.game.packets.base;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.emilieps.game.connection.ClientSession;

public interface PacketHandler {
    /**
     * Handle an incoming packet
     * @param packet The packet being sent
     * @param session The session that sent the packet
     */
    void handle(InboundPacket packet, ClientSession session) throws InvalidProtocolBufferException;
}