package org.genshinimpact.gameserver.connection.kcp;

// Imports
import java.net.InetSocketAddress;

public interface KcpTunnel {
    /**
     * Gets the IP of the tunnel.
     * @return The IP Address.
     */
    InetSocketAddress getAddress();

    /**
     * Writes data to the tunnel.
     * @param bytes The given data.
     */
    void writeData(byte[] bytes);

    /**
     * Closes the tunnel.
     */
    void close();
}