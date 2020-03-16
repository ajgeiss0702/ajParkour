package us.ajg0702.parkour.api;

import org.bukkit.entity.Player;

import us.ajg0702.parkour.game.Manager;

public class AjPakour {
	
	/**
	 * Gets the parkour manager
	 * @return The instance of the parkour manager
	 */
	public static Manager getManager() {
		return Manager.getInstance();
	}
	
	
	/**
	 * Checks if a player is in the parkour
	 * @param ply The player
	 * @return A boolean. True if they are in parkour, false if they are not.
	 */
	public static boolean playerInParkour(Player ply) {
		return Manager.getInstance().getPlayer(ply) != null;
	}
	
}
