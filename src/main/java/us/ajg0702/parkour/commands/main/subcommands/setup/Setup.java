package us.ajg0702.parkour.commands.main.subcommands.setup;

import net.kyori.adventure.text.Component;
import us.ajg0702.commands.CommandSender;
import us.ajg0702.commands.SubCommand;
import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.setup.InProgressArea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Setup extends SubCommand {
    private final ParkourPlugin plugin;
    public Setup(ParkourPlugin plugin) {
        super("setup", Collections.emptyList(), "ajparkour.setup", "Commands for setting up parkour areas");
        this.plugin = plugin;

        addSubCommand(new Create(plugin));
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        List<String> autoComplete = new ArrayList<>(subCommandAutoComplete(sender, args));

        if(args.length == 1) {
            autoComplete.addAll(plugin.getManager().getAreaNames());
            for (String name : plugin.getSetupManager().getNames()) {
                if(!autoComplete.contains(name)) autoComplete.add(name);
            }
        }

        return autoComplete;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String label) {
        if(subCommandExecute(sender, args, label)) return;

        if(args.length >= 1) {
            String areaName = args[0];
            InProgressArea inProgressArea = plugin.getSetupManager().getOrImport(areaName);

            if(inProgressArea == null) {
                sender.sendMessage(plugin.getMessages().getComponent("setup.area-does-not-exist", "NAME:"+areaName));
                return;
            }

            sender.sendMessage(Component.text("next stuff will go here"));
        } else {
            sender.sendMessage(Component.text("Setup commands :)"));
        }
    }
}
