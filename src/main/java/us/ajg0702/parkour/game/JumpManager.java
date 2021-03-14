package us.ajg0702.parkour.game;

import us.ajg0702.parkour.Main;
import us.ajg0702.utils.spigot.ConfigFile;

import java.util.Locale;

public class JumpManager {
    private static JumpManager instance;
    public static JumpManager getInstance() {
        return instance;
    }
    public static JumpManager getInstance(Main pl) {
        if(instance == null) instance = new JumpManager(pl);
        return instance;
    }


    private final ConfigFile jumpConfig;
    private JumpManager(Main pl) {
        jumpConfig = new ConfigFile(pl, "jumps.yml");
        reload();
    }

    public void reload() {
        jumpConfig.reload();
        for(Difficulty d : Difficulty.values()) {
            if(d == Difficulty.BALANCED) continue;
            String dl = d.toString().toLowerCase(Locale.ROOT);
            d.setMin(getInt(dl+"-min"));
            d.setMax(getInt(dl+"-max"));
        }
    }

    public int getInt(String key) {
        return jumpConfig.getInt(key);
    }

    public int getBalancedStart(Difficulty d) {
        return getInt("balanced-start-"+d.toString().toLowerCase(Locale.ROOT));
    }
}
