package us.ajg0702.parkour.commands.main;

import org.bukkit.entity.Player;
import us.ajg0702.commands.BaseCommand;
import us.ajg0702.commands.CommandSender;
import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.commands.main.subcommands.End;
import us.ajg0702.parkour.commands.main.subcommands.Reload;
import us.ajg0702.parkour.commands.main.subcommands.Start;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainCommand extends BaseCommand {
    private final ParkourPlugin plugin;
    public MainCommand(ParkourPlugin plugin) {
        super("ajparkour", Arrays.asList("ap", "ajp", "ajpk", "apk"), null, "Main command for ajParkour");
        this.plugin = plugin;

        addSubCommand(new Start(plugin));
        addSubCommand(new End(plugin));
        addSubCommand(new Reload(plugin));
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        return subCommandAutoComplete(sender, args);
    }

    @Override
    public void execute(CommandSender sender, String[] args, String label) {
        subCommandExecute(sender, args, label);
    }
}
