package org.genshinimpact.gameserver.packets;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import org.genshinimpact.gameserver.connection.ClientSession;

public interface RecvPacket {
    /**
     * Handle an incoming packet.
     * @param session The session that sent the packet.
     * @param header The packet's header.
     * @param data The packet's data.
     */
    void handle(ClientSession session, byte[] header, byte[] data) throws InvalidProtocolBufferException;

    /**
     * Gets the packet id.
     * @return The packet id.
     */
    int getCode();
}