package org.emilieps.game.packets.base;

// Imports
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PacketOpcode {
    /** Opcode for the base/handler */
    int value();

    /** HANDLER ONLY - will disable this handler from being registered */
    boolean disabled() default false;
}