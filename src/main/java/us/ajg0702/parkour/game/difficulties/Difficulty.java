package us.ajg0702.parkour.game.difficulties;

public interface Difficulty {
    int getMaximumDistance(int score);
    int getMinimumDistance(int score);
}
