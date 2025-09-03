package org.emilieps.game.packets;

// Imports
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.emilieps.Application;
import org.emilieps.game.packets.base.PacketHandler;
import org.emilieps.game.packets.base.PacketOpcode;

@Getter
public final class PacketManager {
    private final Map<Integer, PacketHandler> handlers = new HashMap<>();

    public PacketManager(Class<? extends PacketHandler> handlerClass) {
        this.registerHandlers(handlerClass);
    }

    private void registerHandlers(Class<? extends PacketHandler> handlerClass) {
        var handlerClasses = Application.getReflector().getSubTypesOf(handlerClass);
        for (Class<? extends PacketHandler> clazz : handlerClasses) {
            try {
                if (clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers()))
                    continue;
                PacketOpcode annotation = clazz.getAnnotation(PacketOpcode.class);

                if (annotation == null || annotation.disabled())
                    continue;

                PacketHandler instance = clazz.getDeclaredConstructor().newInstance();
                int opcode = annotation.value();
                if (handlers.containsKey(opcode))
                    continue;

                handlers.put(opcode, instance);
            } catch (Exception ignored) {

            }
        }

        Application.getLogger().info(Application.getTranslations().get("console", "totalpackets", handlers.size()));
    }
}