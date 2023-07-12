package us.ajg0702.parkour.utils;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.block.Block;
import us.ajg0702.utils.spigot.VersionSupport;

public class Utils {
    public static void place(XMaterial material, WorldPosition position) {
        Block block = position.getLocation().getBlock();
        block.setType(material.parseMaterial(), false);
        if(VersionSupport.getMinorVersion() < 13) {
            //noinspection deprecation
            block.setData(material.getData(), false);
        }
    }
}
