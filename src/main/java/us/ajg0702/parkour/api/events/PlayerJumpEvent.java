package us.ajg0702.parkour.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import us.ajg0702.parkour.game.PkPlayer;

/**
 * Event called when a player makes a jump
 *
 */
public class PlayerJumpEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    PkPlayer ply;
    public PlayerJumpEvent(PkPlayer p) {
    	this.ply = p;
    }
    
    /**
     * Gets the PkPlayer class associated with this player
     * @return the PkPlayer
     */
    public PkPlayer getPkPlayer() {
    	return ply;
    }
    
    /**
     * The player that jumped
     * @return The player
     */
    public Player getPlayer() {
    	return ply.getPlayer();
    }
    
    /**
     * The current score of the player after jumping.
     * @return The score
     */
    public int getCurrentScore() {
    	return ply.getScore();
    }
}	
