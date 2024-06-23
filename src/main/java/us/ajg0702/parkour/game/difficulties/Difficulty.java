package us.ajg0702.parkour.game.difficulties;

public interface Difficulty {
    String getName();
    int getMaximumDistance(int score);
    int getMinimumDistance(int score);
}
