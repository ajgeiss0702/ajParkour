package us.ajg0702.parkour.utils;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.block.Block;
import org.bukkit.util.NumberConversions;
import us.ajg0702.utils.spigot.VersionSupport;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static void place(XMaterial material, WorldPosition position) {
        Block block = position.getLocation().getBlock();
        block.setType(material.parseMaterial(), false);
        if(VersionSupport.getMinorVersion() < 13) {
            //noinspection deprecation
            block.setData(material.getData(), false);
        }
    }

    public static List<WorldPosition> getBlocksInLine(WorldPosition pos1, WorldPosition pos2) {
        double x = pos2.getX() - pos1.getX();
        double y = pos2.getY() - pos1.getY();
        double z = pos2.getZ() - pos1.getZ();

        double length = Math.sqrt(NumberConversions.square(x) + NumberConversions.square(y) + NumberConversions.square(z));

        x /= length;
        y /= length;
        z /= length;

        List<WorldPosition> positions = new ArrayList<>();

        for (double i = 0; i < length; i += 1) {
            // Since the rate can be any number it's possible to get a higher number than
            // the length in the last loop.
            if (i > length) i = length;
            int pX = (int) (x * i) + pos1.getX();
            int pY = (int) (y * i) + pos1.getY();
            int pZ = (int) (z * i) + pos1.getZ();
            positions.add(new WorldPosition(pos1.getWorldName(), pX, pY, pZ));
        }
        return positions;
//        double xSlope = (pos1.getX() - pos2.getX());
//        double ySlope = (pos1.getY() - pos2.getY()) / xSlope;
//        double zSlope = (pos1.getZ() - pos2.getZ()) / xSlope;
//        double y = pos1.getY();
//        double z = pos2.getZ();
//        double interval = 1 / (Math.abs(ySlope) > Math.abs(zSlope) ? ySlope : zSlope);
//
//        List<WorldPosition> positions = new ArrayList<>();
//
//        int guard = 0;
//
//        for (double x = pos1.getX(); Math.abs(x - pos1.getX()) < Math.abs(xSlope); x += interval, y += ySlope, z += zSlope, guard++) {
//            if(guard > 1000) {
//                System.out.println("Stopped by guard");
//                continue;
//            }
//            positions.add(
//                    new WorldPosition(pos1.getWorldName(), (int) x, (int) y, (int) z)
//            );
//        }
//        return positions;
    }
}
