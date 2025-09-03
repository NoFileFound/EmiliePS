package org.emilieps.game.connection;

// Imports
import io.netty.buffer.ByteBuf;
import org.emilieps.game.packets.base.BadPacketException;

public interface KcpChannel {
    /**
     * Handles when the tunnel establishes connection with the server.
     * @param tunnel The given tunnel.
     */
    void onConnect(KcpTunnel tunnel);

    /**
     * Handles when the tunnel sends data to the server.
     * @param bytes The data sent.
     */
    void onReceive(ByteBuf data) throws BadPacketException;

    /**
     * Handles when the tunnel closes connection.
     */
    void onClose();
}