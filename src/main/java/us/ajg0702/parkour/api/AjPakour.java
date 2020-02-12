package us.ajg0702.parkour.api;

import us.ajg0702.parkour.game.Manager;

public class AjPakour {
	
	/**
	 * Gets the parkour manager
	 * @return The instance of the parkour manager
	 */
	public static Manager getManager() {
		return Manager.getInstance();
	}
	
}
