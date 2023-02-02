package us.ajg0702.parkour;

import org.bukkit.plugin.java.JavaPlugin;

public class ParkourPlugin extends JavaPlugin {
    @Override
    public void onEnable() {

        getLogger().info("ajParkour v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {

        getLogger().info("ajParkour disabled.");
    }
}
