package org.genshinimpact.database.embeds;

// Imports
import dev.morphia.annotations.Embedded;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embedded
public final class FatigueRemind {
    private List<Integer> durations;
    private String nickname;
    private int reset_point;
}