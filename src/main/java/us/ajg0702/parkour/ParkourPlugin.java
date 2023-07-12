package us.ajg0702.parkour;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurateException;
import us.ajg0702.commands.platforms.bukkit.BukkitCommand;
import us.ajg0702.commands.platforms.bukkit.BukkitSender;
import us.ajg0702.parkour.commands.main.MainCommand;
import us.ajg0702.parkour.game.Manager;
import us.ajg0702.utils.common.Config;
import us.ajg0702.utils.common.Messages;

import java.util.logging.Level;

public class ParkourPlugin extends JavaPlugin {

    private Manager manager;

    private Config config;

    @Override
    public void onEnable() {

        try {
            config = new Config(getDataFolder(), getLogger());
        } catch (ConfigurateException e) {
            getLogger().log(Level.SEVERE, "An error occurred while loading your config:", e);
            getLogger().severe("Disabling because the config failed to load. See above error on why.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        manager = new Manager(this);

        Bukkit.getPluginManager().registerEvents(new GameListeners(this), this);

        BukkitCommand bukkitMainCommand = new BukkitCommand(new MainCommand(this));
        bukkitMainCommand.register(this);

        BukkitSender.setAdventure(getAdventure());

        getLogger().info("ajParkour v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        if(manager != null) manager.endAll();
        getLogger().info("ajParkour disabled.");
    }

    public Config getAConfig() {
        return config;
    }

    public Manager getManager() {
        return manager;
    }

    @Override
    public FileConfiguration getConfig() {
        throw new IllegalStateException("Bukkit config system is not used for ajParkour! Use the getAConfig method instead!");
    }

    private static BukkitAudiences adventure;
    public BukkitAudiences getAdventure() {
        if(adventure == null) {
            adventure = BukkitAudiences.create(this);
        }
        return adventure;
    }

    private static MiniMessage miniMessage;
    public static MiniMessage getMiniMessage() {
        if(miniMessage == null) {
            miniMessage = MiniMessage.miniMessage();
        }
        return miniMessage;
    }

    public static Component message(String miniMessage) {
        return getMiniMessage().deserialize(Messages.color(miniMessage));
    }
}
