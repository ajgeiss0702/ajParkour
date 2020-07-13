package us.ajg0702.parkour.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import us.ajg0702.parkour.game.PkPlayer;

/**
 * Event that is called when a player starts parkour
 */
public class PlayerStartParkourEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    
    PkPlayer ply;
    
    public PlayerStartParkourEvent(PkPlayer p) {
    	this.ply = p;
    }
    
    public PkPlayer getParkourPlayer() {
    	return ply;
    }
    public Player getPlayer() {
    	return ply.getPlayer();
    }
	
}
