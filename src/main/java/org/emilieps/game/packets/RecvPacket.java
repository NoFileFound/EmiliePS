package org.emilieps.game.packets;

// Imports
import org.emilieps.game.connection.ClientSession;

public interface RecvPacket {
    /**
     * The packet's opcode.
     */
    int getId();

    /**
     * Handling the packet.
     * @param session The given session.
     * @param header The packet's header.
     * @param payload The packet's payload.
     */
    void handle(ClientSession session, byte[] header, byte[] payload);
}