package org.genshinimpact.gameserver.game;

// Imports
import lombok.Getter;

// Protocol buffers;
import org.generated.protobuf.SceneEntityInfoOuterClass.SceneEntityInfo;

public abstract class Entity {
    /**
     * Gets the entity id.
     */
    @Getter protected int entityId;

    /**
     * Gets the proto object of the entity.
     * @return The protocol buffer object of the entity.
     */
    public SceneEntityInfo toProto() {
        return null;
    }
}