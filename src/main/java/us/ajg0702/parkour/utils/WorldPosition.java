package us.ajg0702.parkour.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public class WorldPosition {

    private final String world;
    private final int x;
    private final int y;
    private final int z;

    public WorldPosition(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public WorldPosition(Location location) {
        this.world = location.getWorld().getName();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    public boolean worldExists() {
        return getWorld() != null;
    }

    public @Nullable World getWorld() {
        return Bukkit.getWorld(getWorldName());
    }

    public Location getLocation() {
        return new Location(getWorld(), x, y, z);
    }

    public String getWorldName() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public WorldPosition getRelative(int xChange, int yChange, int zChange) {
        return new WorldPosition(
                world,
                x + xChange,
                y + yChange,
                z + zChange
        );
    }
}
