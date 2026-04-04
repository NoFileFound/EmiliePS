package org.genshinimpact.gameserver.game.player;

// Imports
import dev.morphia.annotations.Entity;
import lombok.Getter;

// Protocol buffers
import org.generated.protobuf.VectorOuterClass.Vector;

@Entity("PlayerPosition")
@Getter
public final class PlayerPosition {
    public int x;
    public int y;
    public int z;

    /**
     * Creates a new instance of player position.
     */
    public PlayerPosition() {
        this.x = 2747;
        this.y = 194;
        this.z = -1719;
    }

    /**
     * Creates a new instance of player position.
     * @param x The X-axis.
     * @param y The Y-axis.
     * @param z The Z-axis.
     */
    public PlayerPosition(int x, int y, int z) {
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