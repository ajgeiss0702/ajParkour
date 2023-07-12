package us.ajg0702.parkour.game.difficulties;

public class Difficulty {
    private final int minimumDistance;
    private final int maximumDistance;

    public Difficulty(int minimumDistance, int maximumDistance) {
        this.minimumDistance = minimumDistance;
        this.maximumDistance = maximumDistance;
    }

    public int getMinimumDistance() {
        return minimumDistance;
    }

    public int getMaximumDistance() {
        return maximumDistance;
    }
}
