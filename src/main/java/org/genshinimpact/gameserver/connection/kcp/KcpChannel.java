package org.genshinimpact.gameserver.connection.kcp;

// Imports
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.buffer.ByteBuf;
import org.genshinimpact.gameserver.packets.BadPacketException;

public interface KcpChannel {
    /**
     * Handles when the tunnel establishes connection with the server.
     * @param tunnel The given tunnel.
     */
    void onConnect(KcpTunnel tunnel);

    /**
     * Handles when the tunnel sends data to the server.
     * @param data The data sent.
     */
    void onReceive(ByteBuf data) throws BadPacketException, InvalidProtocolBufferException;

    /**
     * Handles when the tunnel closes connection.
     */
    void onClose();
}