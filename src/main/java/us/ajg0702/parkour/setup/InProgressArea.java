package us.ajg0702.parkour.setup;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public class InProgressArea {

    private final String name;

    @Nullable
    private Location pos1;
    @Nullable
    private Location pos2;
    @Nullable
    private Location fallPos;
    @Nullable
    private String difficulty;

    public InProgressArea(String name) {
        this.name = name;
    }

    public InProgressArea(String name, @Nullable Location pos1, @Nullable Location pos2, @Nullable Location fallPos, @Nullable String difficulty) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.fallPos = fallPos;
        this.difficulty = difficulty;
    }

    public String getName() {
        return name;
    }

    public @Nullable Location getPos1() {
        return pos1;
    }

    public @Nullable Location getPos2() {
        return pos2;
    }

    public @Nullable Location getFallPos() {
        return fallPos;
    }

    public @Nullable String getDifficultyString() {
        return difficulty;
    }

    public void setPos1(@Nullable Location pos1) {
        this.pos1 = pos1;
    }

    public void setPos2(@Nullable Location pos2) {
        this.pos2 = pos2;
    }

    public void setFallPos(@Nullable Location fallPos) {
        this.fallPos = fallPos;
    }

    public void setDifficultyString(@Nullable String difficulty) {
        this.difficulty = difficulty;
    }
}
