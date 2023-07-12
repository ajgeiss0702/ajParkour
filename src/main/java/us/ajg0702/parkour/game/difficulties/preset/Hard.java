package us.ajg0702.parkour.game.difficulties.preset;

import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.game.difficulties.StaticDifficulty;

public class Hard extends StaticDifficulty {
    private final ParkourPlugin plugin;

    public Hard(ParkourPlugin plugin) {
        super(plugin.getJumpsConfig().getInt("hard-min"), plugin.getJumpsConfig().getInt("hard-max"));
        this.plugin = plugin;
    }
}
