package us.ajg0702.parkour.commands.main.subcommands.debug;

import us.ajg0702.commands.CommandSender;
import us.ajg0702.commands.SubCommand;
import us.ajg0702.parkour.ParkourPlugin;

import java.util.Collections;
import java.util.List;

import static us.ajg0702.parkour.ParkourPlugin.message;


public class DebugCommands extends SubCommand {
    private final ParkourPlugin plugin;
    public DebugCommands(ParkourPlugin plugin) {
        super("debug", Collections.emptyList(), "ajparkour.debug", "debug commands");
        this.plugin = plugin;
        setShowInTabComplete(false);

        addSubCommand(new Line());
        addSubCommand(new Papi());
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        if(args.length == 1) {
            return filterCompletion(subCommandAutoComplete(sender, args), args[0]);
        } else {
            return subCommandAutoComplete(sender, args);
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args, String label) {
        if(!checkPermission(sender)) {
            sender.sendMessage(message("&cNo permission!"));
            return;
        }

        if(!subCommandExecute(sender, args, label)) {
            sender.sendMessage(message("&cInvalid subcommand!"));
        }
    }
}
