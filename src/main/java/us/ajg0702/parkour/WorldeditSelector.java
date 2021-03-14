package us.ajg0702.parkour;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;

/**
 * I had to split this into its own class because for some reason eclipse didnt like it in with the rest of the commands.
 * 
 */
public class WorldeditSelector {

	public static boolean bruh(WorldEditPlugin wep, CommandSender sender, Messages msgs, HashMap<String, Object> editing) {
		Player sply = (Player) sender;
		LocalSession s = wep.getSession(sply);
		BlockVector3 max;
		BlockVector3 min;
		try {
			max = s.getSelection(s.getSelectionWorld()).getMaximumPoint();
			min = s.getSelection(s.getSelectionWorld()).getMinimumPoint();
		} catch (IncompleteRegionException e) {
			sender.sendMessage(msgs.get("setup.we.incomplete-selection", sply));
			return true;
		}
		
		Location pos1 = new Location(Bukkit.getWorld(s.getSelectionWorld().getName()), max.getX(), max.getY(), max.getZ());
		Location pos2 = new Location(Bukkit.getWorld(s.getSelectionWorld().getName()), min.getX(), min.getY(), min.getZ());
		
		editing.put("pos1", pos1);
		editing.put("pos2", pos2);
		sender.sendMessage(msgs.get("setup.set.we", sply));
		return true;
	}

}
