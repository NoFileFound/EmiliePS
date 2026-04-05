package org.genshinimpact.gameserver.game.player;

// Imports
import dev.morphia.annotations.Entity;
import lombok.Getter;

// Protocol buffers
import org.generated.protobuf.VectorOuterClass.Vector;

@Entity("PlayerPosition")
@Getter
public final class PlayerPosition {
    private float x;
    private float y;
    private float z;

    /**
     * Creates a new instance of position.
     */
    public PlayerPosition() {
        this.x = 2747.6f;
        this.y = 194.7f;
        this.z = -1719.4f;
    }

    /**
     * Creates a new instance of position.
     * @param x The X-axis.
     * @param y The Y-axis.
     * @param z The Z-axis.
     */
    public PlayerPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Changes the player's position.
     * @param x The X axis.
     * @param y The Y axis.
     * @param z The Z axis.
     */
    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Returns the protobuf of the player position.
     * @return The protobuf Vector of the player position.
     */
    public Vector toProto() {
        return Vector.newBuilder().setX(this.x).setY(this.y).setZ(this.z).build();
    }
}