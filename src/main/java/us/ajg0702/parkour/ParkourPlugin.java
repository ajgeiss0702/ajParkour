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
import us.ajg0702.parkour.game.difficulties.DifficultyManager;
import us.ajg0702.utils.common.Config;
import us.ajg0702.utils.common.ConfigFile;
import us.ajg0702.utils.common.Messages;
import us.ajg0702.utils.common.SimpleConfig;

import java.util.HashMap;
import java.util.logging.Level;

public class ParkourPlugin extends JavaPlugin {

    private Manager manager;

    private Config config;

    private ConfigFile jumpsConfig;

    private SimpleConfig positionsConfig;

    private DifficultyManager difficultyManager;

    private Messages messages;

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
        try {
            jumpsConfig = new ConfigFile(getDataFolder(), getLogger(), "jumps.yml");
        } catch (ConfigurateException e) {
            getLogger().log(Level.SEVERE, "An error occurred while loading your jumps config:", e);
            getLogger().severe("Disabling because the jumps config failed to load. See above error on why.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        try {
            String positionsHeader =
                    "This is where the plugin stores all the areas and portals.\n" +
                            "Be careful when editing this file, it could cause some areas or portals to fail to load!";
            positionsConfig = new SimpleConfig(getDataFolder(), "positions.yml", getLogger(), positionsHeader);
        } catch (ConfigurateException e) {
            getLogger().log(Level.SEVERE, "An error occurred while loading your positions (areas/portals) config:", e);
            getLogger().severe("Disabling because the positions (areas/portals) config failed to load. See above error on why.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        difficultyManager = new DifficultyManager(this);

        manager = new Manager(this);

        messages = new Messages(getDataFolder(), getLogger(), new HashMap<>());

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

    public ConfigFile getJumpsConfig() {
        return jumpsConfig;
    }

    public Manager getManager() {
        return manager;
    }

    public DifficultyManager getDifficultyManager() {
        return difficultyManager;
    }

    public SimpleConfig getPositionsConfig() {
        return positionsConfig;
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
