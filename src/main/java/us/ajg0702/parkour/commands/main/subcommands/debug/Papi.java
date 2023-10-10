package us.ajg0702.parkour.commands.main.subcommands.debug;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import us.ajg0702.commands.CommandSender;
import us.ajg0702.commands.SubCommand;
import us.ajg0702.parkour.ParkourPlugin;

import java.util.Collections;
import java.util.List;

import static us.ajg0702.utils.common.Messages.setPlaceholders;

public class Papi extends SubCommand {
    public Papi() {
        super("papi", Collections.emptyList(), "ajparkour.debug.papi", "Test PAPI parsing with components");
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args, String label) {
        String string = String.join(" ", args);
        Component component = ParkourPlugin.getMiniMessage().deserialize(string);

        sender.sendMessage(setPlaceholders((Player) sender.getHandle(), component));
    }
}
