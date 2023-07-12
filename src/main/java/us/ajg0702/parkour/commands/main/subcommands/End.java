package us.ajg0702.parkour.commands.main.subcommands;

import org.bukkit.entity.Player;
import us.ajg0702.commands.CommandSender;
import us.ajg0702.commands.SubCommand;
import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.game.ParkourPlayer;

import java.util.Collections;
import java.util.List;

import static us.ajg0702.parkour.ParkourPlugin.message;

public class End extends SubCommand {
    private final ParkourPlugin plugin;
    public End(ParkourPlugin plugin) {
        super("end", Collections.emptyList(), null, "End your parkour");
        this.plugin = plugin;
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args, String label) {
        ParkourPlayer player = plugin.getManager().getPlayer((Player) sender.getHandle());
        if(player == null) {
            sender.sendMessage(message("&cYou arent in parkour!"));
            return;
        }
        player.end();
    }
}
