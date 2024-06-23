package us.ajg0702.parkour.game.difficulties;

public abstract class StaticDifficulty implements Difficulty {
    private final int minimumDistance;
    private final int maximumDistance;

    public StaticDifficulty(int minimumDistance, int maximumDistance) {
        this.minimumDistance = minimumDistance;
        this.maximumDistance = maximumDistance;
    }

    public abstract String getName();

    public int getMinimumDistance(int score) {
        return minimumDistance;
    }

    public int getMaximumDistance(int score) {
        return maximumDistance;
    }
}
