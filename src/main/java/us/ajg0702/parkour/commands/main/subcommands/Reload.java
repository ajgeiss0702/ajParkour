package us.ajg0702.parkour.commands.main.subcommands;

import org.spongepowered.configurate.ConfigurateException;
import us.ajg0702.commands.CommandSender;
import us.ajg0702.commands.SubCommand;
import us.ajg0702.parkour.ParkourPlugin;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static us.ajg0702.parkour.ParkourPlugin.message;

public class Reload extends SubCommand {
    private final ParkourPlugin plugin;
    public Reload(ParkourPlugin plugin) {
        super("reload", Collections.emptyList(), "ajparkour.reload", "Reload the configs");
        this.plugin = plugin;
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args, String label) {
        if(!checkPermission(sender)) {
            sender.sendMessage(message("&cnoperm"));
            return;
        }
        try {
            plugin.getAConfig().reload();
            sender.sendMessage(message("&asuccess"));
        } catch (ConfigurateException e) {
            plugin.getLogger().log(Level.WARNING, "An error ocurred while reloading the config");
            sender.sendMessage(message("&cfailed (see console)"));
        }
    }
}
