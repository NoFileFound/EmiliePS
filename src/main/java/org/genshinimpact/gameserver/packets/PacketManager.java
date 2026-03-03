package org.genshinimpact.gameserver.packets;

// Imports
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.genshinimpact.bootstrap.AppBootstrap;

@Getter
public final class PacketManager {
    private final Map<Integer, PacketHandler> handlers = new HashMap<>();

    public PacketManager(Class<? extends PacketHandler> handlerClass) {
        this.registerHandlers(handlerClass);
    }

    private void registerHandlers(Class<? extends PacketHandler> handlerClass) {
        var handlerClasses = AppBootstrap.getReflector().getSubTypesOf(handlerClass);
        for(Class<? extends PacketHandler> clazz : handlerClasses) {
            try {
                if(clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers()))
                    continue;

                PacketHandler instance = clazz.getDeclaredConstructor().newInstance();
                int opcode = instance.getCode();

                if(handlers.containsKey(opcode))
                    continue;

                handlers.put(opcode, instance);
            } catch(Exception ignored) {}
        }

        AppBootstrap.getLogger().info("Implemented total packets: {}", handlers.size());
    }
}