package us.ajg0702.parkour.game.difficulties.preset;

import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.game.difficulties.StaticDifficulty;

public class Medium extends StaticDifficulty {
    private final ParkourPlugin plugin;

    public Medium(ParkourPlugin plugin) {
        super(plugin.getJumpsConfig().getInt("medium-min"), plugin.getJumpsConfig().getInt("medium-max"));
        this.plugin = plugin;
    }
}
