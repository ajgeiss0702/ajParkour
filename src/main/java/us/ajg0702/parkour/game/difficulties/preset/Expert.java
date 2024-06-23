package us.ajg0702.parkour.game.difficulties.preset;

import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.game.difficulties.StaticDifficulty;

public class Expert extends StaticDifficulty {
    private final ParkourPlugin plugin;

    public Expert(ParkourPlugin plugin) {
        super(plugin.getJumpsConfig().getInt("expert-min"), plugin.getJumpsConfig().getInt("expert-max"));
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "expert";
    }
}
