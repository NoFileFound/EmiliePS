package org.emilieps.game.connection;

public interface KcpChannelHandler {
    /**
     * Handles the session when connects to the server.
     * @param session The given session.
     */
    void onConnected(ClientSession session);

    /**
     * Handles the session when disconnects from the server.
     * @param session The given session.
     */
    void onClosed(ClientSession session);

    /**
     * Handles the session when sends data to the server.
     * @param session The given session.
     * @param bytes The session's payload (data).
     */
    void onMessageReceived(ClientSession session, byte[] bytes);

    /**
     * Handles the session when exception occurs.
     * @param ex The threw exception.
     */
    void exceptionCaught(Throwable ex);
}