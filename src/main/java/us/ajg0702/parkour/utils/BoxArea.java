package us.ajg0702.parkour.utils;

import us.ajg0702.utils.common.GenUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BoxArea {
    private final WorldPosition position1;
    private final WorldPosition position2;

    private final int minX;
    private final int maxX;

    private final int minY;
    private final int maxY;

    private final int minZ;
    private final int maxZ;

    public BoxArea(WorldPosition position1, WorldPosition position2) {
        this.position1 = position1;
        this.position2 = position2;
        if(!Objects.equals(position1.getWorldName(), position2.getWorldName())) {
            throw new IllegalArgumentException("Positions must be in the same world!");
        }

        minX = Math.min(position1.getX(), position2.getX());
        maxX = Math.max(position1.getX(), position2.getX());

        minY = Math.min(position1.getY(), position2.getY());
        maxY = Math.max(position1.getY(), position2.getY());

        minZ = Math.min(position1.getZ(), position2.getZ());
        maxZ = Math.max(position1.getZ(), position2.getZ());
    }

    public boolean isInside(WorldPosition position) {
        if(!Objects.equals(position.getWorldName(), position1.getWorldName())) return false;
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();

        if(x < minX || x > maxX) return false;
        if(y < minY || y > maxY) return false;
        return z >= minZ && z <= maxZ;
    }

    public WorldPosition randomPoint(boolean withMargin) {
        int margin = withMargin ? 5 : 0;
        int x = GenUtils.randomInt(minX + margin, maxX - margin);
        int y = GenUtils.randomInt(minY + margin, maxY - margin);
        int z = GenUtils.randomInt(minZ + margin, maxZ - margin);

        return new WorldPosition(position2.getWorldName(), x, y, z);
    }

    public WorldPosition randomPoint() {
        return randomPoint(true);
    }

    public int getClosestWallDistance(WorldPosition position) {
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();
        List<Integer> distances = Arrays.asList(
                distance(x, minX) * (x < minX ? -1 : 1),
                distance(x, maxX) * (x > maxX ? -1 : 1),

                distance(y, minY) * (y < minY ? -1 : 1),
                distance(y, maxY) * (y > maxY ? -1 : 1),

                distance(z, minZ) * (z < minZ ? -1 : 1),
                distance(z, maxZ) * (z > maxZ ? -1 : 1)
        );

        int lowest = Integer.MAX_VALUE;
        for (Integer distance : distances) {
            if(distance < lowest) {
                lowest = distance;
            }
        }

        return lowest;
    }

    private int distance(int a, int b) {
        return Math.abs(a - b);
    }

    public WorldPosition getPosition1() {
        return position1;
    }

    public WorldPosition getPosition2() {
        return position2;
    }
}
