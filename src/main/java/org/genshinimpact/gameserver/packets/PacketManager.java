package org.genshinimpact.gameserver.packets;

// Imports
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.genshinimpact.bootstrap.AppBootstrap;

@Getter
public final class PacketManager {
    private final Map<Integer, RecvPacket> handlers = new HashMap<>();
    private final Map<Integer, String> packetNames = new HashMap<>();

    public PacketManager(Class<? extends RecvPacket> handlerClass) {
        registerHandlers(handlerClass);
        registerNames(PacketIdentifiers.Receive.class);
        registerNames(PacketIdentifiers.Send.class);
    }

    private void registerNames(Class<?> clazz) {
        for(Field field : clazz.getDeclaredFields()) {
            if(field.getType() != int.class)
                continue;

            try {
                int value = field.getInt(null);
                packetNames.put(value, field.getName());
            } catch(IllegalAccessException ignored) {}
        }
    }

    private void registerHandlers(Class<? extends RecvPacket> handlerClass) {
        var handlerClasses = AppBootstrap.getReflector().getSubTypesOf(handlerClass);
        for(Class<? extends RecvPacket> clazz : handlerClasses) {
            try {
                if(clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers()))
                    continue;

                RecvPacket instance = clazz.getDeclaredConstructor().newInstance();
                int opcode = instance.getCode();

                if(handlers.containsKey(opcode))
                    continue;

                handlers.put(opcode, instance);
            } catch(Exception ignored) {}
        }

        AppBootstrap.getLogger().info("Implemented total packets: {}", handlers.size());
    }

    /**
     * Gets the packet name from packet id.
     * @param opcode The packet's id.
     * @return The packet name.
     */
    public String getPacketName(int opcode) {
        return packetNames.getOrDefault(opcode, String.format("UNKNOWN_PACKET (%s)", opcode));
    }
}