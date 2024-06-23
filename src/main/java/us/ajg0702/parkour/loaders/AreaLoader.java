package us.ajg0702.parkour.loaders;

import org.bukkit.Location;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.game.ParkourArea;
import us.ajg0702.parkour.game.difficulties.Difficulty;
import us.ajg0702.parkour.game.difficulties.DifficultyManager;
import us.ajg0702.parkour.utils.BoxArea;
import us.ajg0702.parkour.utils.WorldPosition;
import us.ajg0702.utils.common.ConfigFile;
import us.ajg0702.utils.common.SimpleConfig;
import us.ajg0702.utils.spigot.LocUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class AreaLoader {
    public static List<ParkourArea> loadAreas(ParkourPlugin plugin, SimpleConfig positions, DifficultyManager difficultyManager) {
        List<ParkourArea> createdAreas = new ArrayList<>();
        Map<Object, CommentedConfigurationNode> areas = positions.getNode("areas").childrenMap();

        plugin.getLogger().info(areas.size() + " areas " + positions.getNode().childrenMap().keySet());

        for (Map.Entry<Object, CommentedConfigurationNode> areaEntry : areas.entrySet()) {
            String name = (String) areaEntry.getKey();
            CommentedConfigurationNode areaNode = areaEntry.getValue();

            plugin.getLogger().info("Processing " + name);

            String pos1Raw = areaNode.node("pos1").getString();
            String pos2Raw = areaNode.node("pos2").getString();
            String difficultyRaw = areaNode.node("difficulty").getString();
            if(pos1Raw == null) {
                plugin.getLogger().warning("Area " + name + " is missing required property 'pos1'. Skipping " + name);
                continue;
            }
            if(pos2Raw == null) {
                plugin.getLogger().warning("Area " + name + " is missing required property 'pos2'. Skipping " + name);
                continue;
            }
            if(difficultyRaw == null) {
                plugin.getLogger().warning("Area " + name + " is missing required property 'difficulty'. Skipping " + name);
                continue;
            }

            try {
                WorldPosition pos1 = WorldPosition.of(LocUtils.stringToLoc(pos1Raw));
                WorldPosition pos2 = WorldPosition.of(LocUtils.stringToLoc(pos2Raw));

                Location fallPosition = LocUtils.stringToLoc(areaNode.node("fallpos").getString());

                BoxArea box = new BoxArea(pos1, pos2);

                Difficulty difficulty = difficultyManager.getNamed(difficultyRaw);


                createdAreas.add(
                        new ParkourArea(plugin, name, box, difficulty, fallPosition)
                );
            } catch(Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error while trying to load area " + name + ":", e);
            }

        }
        return createdAreas;
    }

    public static boolean saveAreas(ParkourPlugin plugin, SimpleConfig positions, List<ParkourArea> areas) {

        for (ParkourArea area : areas) {
            ConfigurationNode node = positions.getNode("areas").node(area.getName());

            try {
                node.node("pos1").set(area.getBox().getPosition1());
                node.node("pos2").set(area.getBox().getPosition2());
                node.node("difficulty").set(area.getDifficulty().getName());

                Location fallPosition = area.getFallPosition();
                if(fallPosition != null) {
                    node.node("fallpos").set(LocUtils.locToString(fallPosition));
                }
            } catch (SerializationException e) {
                plugin.getLogger().log(Level.WARNING, "Error while saving area " + area.getName() + ":", e);
            }
        }

        try {
            positions.save();
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error while saving areas file:", e);
            return false;
        }
    }
}
