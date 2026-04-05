package org.genshinimpact.gameserver.game.player;

// Imports
import dev.morphia.annotations.Entity;
import lombok.Getter;

// Protocol buffers
import org.generated.protobuf.BirthdayOuterClass.Birthday;

@Entity("Birthday")
@Getter
public final class PlayerBirthday {
    private int day;
    private int month;

    /**
     * Creates a new instance of Birthday.
     */
    public PlayerBirthday() {
        this.day = 0;
        this.month = 0;
    }

    /**
     * Creates a new instance of Birthday.
     * @param day The day.
     * @param month The month.
     */
    public PlayerBirthday(int day, int month) {
        this.day = day;
        this.month = month;
    }

    /**
     * Checks if the birthday is already set before.
     * @return True if its already set before or else False.
     */
    public boolean isAlreadySet() {
        return this.day > 0 || this.month > 0;
    }

    /**
     * Changes the player's birthday.
     * @param day The specified day.
     * @param month The specified month.
     */
    public void setBirthday(int day, int month) {
        this.day = day;
        this.month = month;
    }

    /**
     * Returns the protobuf of the birthday.
     * @return The protobuf Birthday of the player's birthday.
     */
    public Birthday toProto() {
        return Birthday.newBuilder().setDay(this.day).setMonth(this.month).build();
    }
}