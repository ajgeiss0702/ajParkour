package us.ajg0702.parkour.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import us.ajg0702.parkour.Main;

public class MaterialParser {

	@SuppressWarnings("deprecation")
	public static void placeBlock(Location loc, String blockname) {
		Material mat;
		int data = -1;
		if(blockname.indexOf(":") != -1 && VersionSupport.getMinorVersion() <= 12) {
			String sd = blockname.split(":")[1];
			if(sd.equalsIgnoreCase("true")) {
				data = Main.random(0, 16);
			} else {
				data = Integer.valueOf(sd);
			}
		}
		try {
			mat = Material.valueOf(blockname.split(":")[0]);
		} catch(Exception e) {
			Bukkit.getLogger().warning("[ajParkour] Could not find block '"+blockname+"'!");
			return;
		}
		loc.getBlock().setType(mat);
		if(data >= 0) {
			loc.getBlock().setData((byte) data);
		}
	}

}
