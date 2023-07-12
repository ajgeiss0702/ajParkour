package us.ajg0702.parkour;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import us.ajg0702.parkour.game.ParkourPlayer;
import us.ajg0702.parkour.utils.WorldPosition;

import java.util.HashMap;
import java.util.UUID;

public class GameListeners implements Listener {
    private final ParkourPlugin plugin;

    private final HashMap<UUID, WorldPosition> moveThrottle = new HashMap<>();

    public GameListeners(ParkourPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        WorldPosition currentPosition = new WorldPosition(e.getTo());
        WorldPosition lastPosition = moveThrottle.get(e.getPlayer().getUniqueId());
        if(currentPosition.equals(lastPosition)) return;
        moveThrottle.put(e.getPlayer().getUniqueId(), currentPosition);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ParkourPlayer player = plugin.getManager().getPlayer(e.getPlayer());
            if(player == null) return;
            player.checkMadeIt();
        });
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        ParkourPlayer player = plugin.getManager().getPlayer(e.getPlayer());
        if(player != null) {
            player.end();
        }
        moveThrottle.remove(e.getPlayer().getUniqueId());
    }
}
