package us.ajg0702.parkour.commands.main.subcommands.debug;

import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import us.ajg0702.commands.CommandSender;
import us.ajg0702.commands.SubCommand;
import us.ajg0702.parkour.utils.Utils;
import us.ajg0702.parkour.utils.WorldPosition;
import us.ajg0702.utils.spigot.LocUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static us.ajg0702.parkour.ParkourPlugin.message;

public class Line extends SubCommand {
    public Line() {
        super("line", Collections.emptyList(), null, "Draw a line");
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        return Arrays.stream(Particle.values()).map(Enum::toString).collect(Collectors.toList());
    }

    @Override
    public void execute(CommandSender sender, String[] args, String label) {
        org.bukkit.command.CommandSender handle = (org.bukkit.command.CommandSender) sender.getHandle();
        if(!(handle instanceof Player)) {
            sender.sendMessage(message("&conly in-game!"));
            return;
        }

        Player player = (Player) handle;

        Location target = player.getLocation().add(player.getLocation().getDirection().multiply(10));

        List<WorldPosition> positions = Utils.getBlocksInLine(
                new WorldPosition(player.getLocation()),
                new WorldPosition(target)
        );

        Particle particle = Particle.NOTE;
        if(args.length > 0) {
            try {
                particle = Particle.valueOf(args[0]);
            } catch (IllegalArgumentException e) {
                player.sendMessage("invalid particle");
            }
        }

        player.getWorld().spawnParticle(Particle.valueOf("CAMPFIRE_COSY_SMOKE"), LocUtils.center(target), 1, 0, 0, 0, 0);
        for (WorldPosition position : positions) {
            player.getWorld().spawnParticle(particle, position.centerLocation() , 1, 0, 0, 0, 0);
        }
    }
}
