package us.ajg0702.parkour;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import us.ajg0702.parkour.game.ParkourPlayer;

import java.util.HashMap;
import java.util.UUID;

public class GameListeners implements Listener {
    private final ParkourPlugin plugin;

    private final HashMap<UUID, Long> moveThrottle = new HashMap<>();

    public GameListeners(ParkourPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        long lastFire = moveThrottle.getOrDefault(e.getPlayer().getUniqueId(), 0L);
        if(System.currentTimeMillis() - lastFire < 50) return;
        moveThrottle.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());

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
