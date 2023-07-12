package us.ajg0702.parkour.commands.main.subcommands;

import org.bukkit.entity.Player;
import us.ajg0702.commands.CommandSender;
import us.ajg0702.commands.SubCommand;
import us.ajg0702.parkour.ParkourPlugin;

import java.util.Collections;
import java.util.List;

public class Start extends SubCommand {
    private final ParkourPlugin plugin;

    public Start(ParkourPlugin plugin) {
        super("start", Collections.emptyList(), null, "Start parkour");
        this.plugin = plugin;
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args, String label) {
        plugin.getManager().start((Player) sender.getHandle());
    }
}
