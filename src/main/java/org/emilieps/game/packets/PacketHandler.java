package org.emilieps.game.packets;

// Imports
import lombok.Getter;
import java.util.HashMap;
import java.util.Map;
import org.emilieps.Application;

@Getter
public final class PacketHandler {
    private final Map<Integer, RecvPacket> handlers;

    public PacketHandler(Class<? extends RecvPacket> handlerClass) {
        this.handlers = new HashMap<>();
        this.registerHandlers(handlerClass);
    }

    public void registerHandlers(Class<? extends RecvPacket> handlerClass) {
        var handlerClasses = Application.getReflector().getSubTypesOf(handlerClass);
        for (Class<? extends RecvPacket> recv : handlerClasses) {
            try {
                RecvPacket instance = recv.getDeclaredConstructor().newInstance();
                this.handlers.put(instance.getId(), instance);
            } catch (Exception ignored) {
            }
        }

        Application.getLogger().info(Application.getTranslationManager().get("console", "totalpackets", this.handlers.size()));
    }
}