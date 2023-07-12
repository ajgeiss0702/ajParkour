package us.ajg0702.parkour.game;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.game.difficulties.Difficulty;
import us.ajg0702.parkour.utils.BoxArea;
import us.ajg0702.parkour.utils.WorldPosition;

import java.util.ArrayList;
import java.util.List;

public class Manager {

    private final ParkourPlugin plugin;

    private final List<ParkourArea> areas = new ArrayList<>();

    public Manager(ParkourPlugin plugin) {
        this.plugin = plugin;
        areas.add(new ParkourArea(
                plugin, "test",
                new BoxArea(
                        new WorldPosition(
                                "world",
                                12, 76, -21
                        ),
                        new WorldPosition(
                                "world",
                                -22, 105, -68
                        )
                ),
                new Difficulty(2, 4)
                ));
    }

    public @Nullable ParkourPlayer getPlayer(Player player) {
        for (ParkourArea area : areas) {
            for (ParkourPlayer areaPlayer : area.getPlayers()) {
                if(areaPlayer.getPlayer().equals(player)) return areaPlayer;
            }
        }
        return null;
    }

    public void endAll() {
        for (ParkourArea area : areas) {
            for (ParkourPlayer player : area.getPlayers()) {
                player.end();
            }
        }
    }

    public void start(Player player) {
        areas.get(0).startPlayer(player);
    }


    public XMaterial getBlockMaterial(String areaName, Player player) {
        return XMaterial.AMETHYST_BLOCK;
    }
}
