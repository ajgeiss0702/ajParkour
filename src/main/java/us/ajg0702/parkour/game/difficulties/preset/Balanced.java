package us.ajg0702.parkour.game.difficulties.preset;

import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.game.difficulties.Difficulty;
import us.ajg0702.parkour.game.difficulties.DifficultyManager;
import us.ajg0702.utils.common.ConfigFile;

public class Balanced implements Difficulty {
    private final ParkourPlugin plugin;

    public Balanced(ParkourPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "balanced";
    }

    @Override
    public int getMaximumDistance(int score) {
        return select(score).getMaximumDistance(score);
    }

    @Override
    public int getMinimumDistance(int score) {
        return select(score).getMinimumDistance(score);
    }

    public Difficulty select(int score) {
        ConfigFile jumpsConfig = plugin.getJumpsConfig();
        DifficultyManager difficultyManager = plugin.getDifficultyManager();

        if(score < jumpsConfig.getInt("balanced-start-medium")) {
            return difficultyManager.getNamed("easy");
        } else if(score < jumpsConfig.getInt("balanced-start-hard")) {
            return difficultyManager.getNamed("medium");
        } else if(score < jumpsConfig.getInt("balanced-start-expert")) {
            return difficultyManager.getNamed("hard");
        } else {
            return difficultyManager.getNamed("expert");
        }
    }
}
