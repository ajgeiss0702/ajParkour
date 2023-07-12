package us.ajg0702.parkour.game;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.loaders.AreaLoader;
import us.ajg0702.parkour.utils.BoxArea;
import us.ajg0702.parkour.utils.WorldPosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Manager {

    private final ParkourPlugin plugin;

    private final List<ParkourArea> areas = new ArrayList<>();

    public Manager(ParkourPlugin plugin) {
        this.plugin = plugin;
        reloadAreas();
//        areas.add(new ParkourArea(
//                plugin, "test",
//                new BoxArea(
//                        new WorldPosition(
//                                "world",
//                                12, 76, -21
//                        ),
//                        new WorldPosition(
//                                "world",
//                                -22, 105, -68
//                        )
//                ),
//                plugin.getDifficultyManager().getNamed("balanced"),
//                null));
    }

    public void reloadAreas() {
        List<ParkourArea> newAreas = AreaLoader.loadAreas(plugin, plugin.getPositionsConfig(), plugin.getDifficultyManager());
        HashMap<String, ParkourArea> existingAreaMap = getAreaMap();

        List<ParkourArea> addedAreas = new ArrayList<>();
        for (ParkourArea newArea : newAreas) {
            if(existingAreaMap.containsKey(newArea.getName())) {
                plugin.getLogger().info("Existing " + newArea.getName());
                ParkourArea existingArea = existingAreaMap.get(newArea.getName());
                existingArea.setBox(newArea.getBox());
                existingArea.setFallPosition(newArea.getFallPosition());
                existingArea.setDifficulty(newArea.getDifficulty());
            } else {
                plugin.getLogger().info("New " + newArea.getName());
                addedAreas.add(newArea);
            }
        }
        areas.addAll(addedAreas);
    }

    public HashMap<String, ParkourArea> getAreaMap() {
        HashMap<String, ParkourArea> areaMap = new HashMap<>();
        for (ParkourArea area : areas) {
            areaMap.put(area.getName(), area);
        }
        return areaMap;
    }

    public Set<String> getAreaNames() {
        return getAreaMap().keySet();
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
