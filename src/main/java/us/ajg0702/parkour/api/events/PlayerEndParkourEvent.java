package us.ajg0702.parkour.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event called when a player falls or gets kicked from the parkour
 *
 */
public class PlayerEndParkourEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    Player ply;
    int fallscore;
    public PlayerEndParkourEvent(Player p, int fallscore) {
    	this.ply = p;
    	this.fallscore = fallscore;
    }
    
    /**
     * Gets the player that fell
     * @return The player that fell
     */
    public Player getPlayer() {
    	return ply;
    }
    
    /**
     * Gets the score the player was at when they fell
     * @return The score they were at when they fell
     */
    public int getFallScore() {
    	return fallscore;
    }
}
