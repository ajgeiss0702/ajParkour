package us.ajg0702.parkour.commands.main.subcommands.setup;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import org.bukkit.entity.Player;
import us.ajg0702.commands.CommandSender;
import us.ajg0702.commands.SubCommand;
import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.setup.InProgressArea;

import java.util.ArrayList;
import java.util.Arrays;
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

        if(autoComplete.size() == 0 && !(args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("createArea"))) {
            if(args.length == 2) {
                autoComplete.addAll(Arrays.asList("pos1", "pos2", "difficulty", "fallpos"));
            } else if(args.length == 3 && args[1].equalsIgnoreCase("difficulty")) {
                autoComplete.addAll(plugin.getDifficultyManager().getNames());
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

            if(args.length > 1) {

                if(!sender.isPlayer()) {
                    sender.sendMessage(plugin.getMessages().getComponent("ingame"));
                    return;
                }

                Player player = (Player) sender.getHandle();

                switch(args[1].toLowerCase()) {
                    case "pos1":
                        inProgressArea.setPos1(player.getLocation());
                        sender.sendMessage(Component.text("set"));
                        return;
                    case "pos2":
                        inProgressArea.setPos2(player.getLocation());
                        sender.sendMessage(Component.text("set"));
                        return;
                    case "difficulty":
                        if(args.length < 3) {
                            sender.sendMessage(
                                    plugin.getMessages().getComponent("setup.difficulty.usage", "LABEL:"+label, "AREA:" + areaName)
                            );
                            return;
                        }
                        String difficulty = args[2].toLowerCase();
                        if(!plugin.getDifficultyManager().hasNamed(difficulty)) {
                            sender.sendMessage(
                                    plugin.getMessages().getComponent("setup.difficulty.invalid-difficulty", "DIFFICULTY:"+difficulty)
                            );
                            return;
                        }
                        inProgressArea.setDifficultyString(difficulty);
                        sender.sendMessage(Component.text("set"));
                        return;
                    case "fallpos":
                        inProgressArea.setFallPos(player.getLocation());
                        sender.sendMessage(Component.text("set"));
                        return;
                }
            }


            sender.sendMessage(
                    plugin.getMessages().getComponent("setup.progress.header", "NAME:"+areaName).appendNewline()
                            .appendNewline()
                            .append(plugin.getMessages().getComponent(inProgressArea.getPos1() == null ? "setup.progress.pos1.unset" : "setup.progress.pos1.set", "NAME:"+areaName)).appendNewline()
                            .append(plugin.getMessages().getComponent(inProgressArea.getPos2() == null ? "setup.progress.pos2.unset" : "setup.progress.pos2.set", "NAME:"+areaName)).appendNewline()
                            .append(plugin.getMessages().getComponent(inProgressArea.getDifficultyString() == null ? "setup.progress.difficulty.unset" : "setup.progress.difficulty.set", "DIFFICULTY:"+inProgressArea.getDifficultyString(), "NAME:"+areaName)).appendNewline()
                            .append(plugin.getMessages().getComponent(inProgressArea.getFallPos() == null ? "setup.progress.fallpos.unset" : "setup.progress.fallpos.set", "NAME:"+areaName))
            );
        } else {
            sender.sendMessage(Component.text("Setup commands :)"));
        }
    }

}
