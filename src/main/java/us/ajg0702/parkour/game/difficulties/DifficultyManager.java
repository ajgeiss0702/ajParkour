package us.ajg0702.parkour.game.difficulties;

import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.game.difficulties.preset.*;

import java.util.HashMap;

public class DifficultyManager {
    private final ParkourPlugin plugin;

    private final HashMap<String, Difficulty> difficulties = new HashMap<>();

    public DifficultyManager(ParkourPlugin plugin) {
        this.plugin = plugin;

        difficulties.put("easy", new Easy(plugin));
        difficulties.put("medium", new Medium(plugin));
        difficulties.put("hard", new Hard(plugin));
        difficulties.put("expert", new Expert(plugin));

        difficulties.put("balanced", new Balanced(plugin));
    }

    public Difficulty getNamed(String name) {
        return difficulties.get(name);
    }
}
