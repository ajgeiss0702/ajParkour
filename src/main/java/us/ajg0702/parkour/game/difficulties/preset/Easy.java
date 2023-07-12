package us.ajg0702.parkour.game.difficulties.preset;

import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.game.difficulties.StaticDifficulty;

public class Easy extends StaticDifficulty {
    private final ParkourPlugin plugin;

    public Easy(ParkourPlugin plugin) {
        super(plugin.getJumpsConfig().getInt("easy-min"), plugin.getJumpsConfig().getInt("easy-max"));
        this.plugin = plugin;
    }
}
