package us.ajg0702.parkour.game;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;
import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.utils.BlockDirection;
import us.ajg0702.parkour.utils.Utils;
import us.ajg0702.parkour.utils.WorldPosition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParkourBlock {
    private final ParkourPlugin plugin;
    private final ParkourArea area;
    private final ParkourPlayer player;

    private WorldPosition position;
    private BlockDirection direction;

    private BlockPossibility selected;

    public ParkourBlock(@Nullable ParkourBlock previous, ParkourPlugin plugin, ParkourArea area, ParkourPlayer player) {
        this.plugin = plugin;
        WorldPosition start = null;
        if(previous == null) {
            while(start == null || start.getLocation().getBlock().getType().isSolid()) {
                start = area.getBox().randomPoint();
            }
            direction = BlockDirection.randomDirection(
                    !plugin.getAConfig().getBoolean("allow-diagonal-jumps")
            );
        } else {
            start = previous.getPosition();
            direction = previous.getDirection();
        }


        List<BlockPossibility> possibilities = new ArrayList<>();
        int minDistance = area.getDifficulty().getMinimumDistance(player.getScore());
        int maxDistance = area.getDifficulty().getMaximumDistance(player.getScore());
        for (int y = -1; y <= 1; y++) {
            int possibleMax = y == 1 ? 4 : 5;
            for (int distance = minDistance; distance <= Math.min(maxDistance, possibleMax); distance++) {
                possibilities.addAll(Arrays.asList(
                        new BlockPossibility(player, previous, start, BlockDirection.NORTH, 0, y, distance * -1),
                        new BlockPossibility(player, previous, start, BlockDirection.EAST, distance, y, 0),
                        new BlockPossibility(player, previous, start, BlockDirection.SOUTH, 0, y, distance),
                        new BlockPossibility(player, previous, start, BlockDirection.WEST, distance * -1, y, 0)
                ));
                if(plugin.getAConfig().getBoolean("allow-diagonal-jumps")) {
                    int dDistance = (int) Math.floor(Math.sqrt((Math.pow(distance, 2) / 2)));
                    possibilities.addAll(Arrays.asList(
                            new BlockPossibility(player, previous, start, BlockDirection.NORTH_EAST, dDistance, y, dDistance * -1),
                            new BlockPossibility(player, previous, start, BlockDirection.SOUTH_EAST, dDistance, y, dDistance),
                            new BlockPossibility(player, previous, start, BlockDirection.SOUTH_WEST, dDistance * -1, y, dDistance),
                            new BlockPossibility(player, previous, start, BlockDirection.NORTH_WEST, dDistance * -1, y, dDistance * -1)
                    ));
                }
            }
        }



        possibilities.sort((a, b) -> b.getScore() - a.getScore());

        int highestScore = possibilities.get(0).getScore();
        List<BlockPossibility> acceptableBlocks = new ArrayList<>();
        for (BlockPossibility possibility : possibilities) {
            if(possibility.getScore() != highestScore) break;
            acceptableBlocks.add(possibility);
        }

        selected = acceptableBlocks.get((int) Math.floor(Math.random() * acceptableBlocks.size()));

        direction = selected.getDirection();
        position = selected.getPosition();

        this.area = area;
        this.player = player;
    }

    public void place() {
        Runnable placeTask =
                () -> Utils.place(plugin.getManager().getBlockMaterial(area.getName(), player.getPlayer()), position);
        if(Bukkit.isPrimaryThread()) {
            placeTask.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, placeTask);
        }
    }

    public void remove() {
        Runnable placeTask =
                () -> position.getLocation().getBlock().setType(Material.AIR, false);
        if(Bukkit.isPrimaryThread()) {
            placeTask.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, placeTask);
        }
    }

    public WorldPosition getPosition() {
        return position;
    }

    public BlockDirection getDirection() {
        return direction;
    }
}
