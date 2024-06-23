package us.ajg0702.parkour.commands.main.subcommands.setup;

import us.ajg0702.commands.CommandSender;
import us.ajg0702.commands.SubCommand;
import us.ajg0702.parkour.ParkourPlugin;

import java.util.Collections;
import java.util.List;

public class Create extends SubCommand {
    private final ParkourPlugin plugin;

    public Create(ParkourPlugin plugin) {
        super("create", Collections.singletonList("createArea"), null, "Create an area for setup.");
        this.plugin = plugin;
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args, String label) {
        if(args.length < 1) {
            sender.sendMessage(plugin.getMessages().getComponent("setup.create.usage", "LABEL:"+label));
            return;
        }

        String areaName = args[0];

        if(plugin.getManager().getAreaNames().contains(areaName) || plugin.getSetupManager().getNames().contains(areaName)) {
            sender.sendMessage(plugin.getMessages().getComponent("setup.create.fail.already-exists", "NAME:"+areaName));
            return;
        }

        plugin.getSetupManager().createInProgressArea(areaName);
        sender.sendMessage(plugin.getMessages().getComponent("setup.create.success", "NAME:"+areaName, "LABEL:"+label));
    }
}
