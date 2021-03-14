package us.ajg0702.parkour.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * The event called before the player starts the parkour. If canceled it will just not start parkour. No messages or anything.
 */
public class PrePlayerStartParkourEvent extends Event implements Cancellable {
	private boolean isCancelled;

	private final Player ply;
    public PrePlayerStartParkourEvent(Player p) {
        this.ply = p;
    }
    
    /**
     * Get the player that is going to start parkour
     * @return The player that is going to start parkour
     */
    public Player getPlayer() {
    	return ply;
    }

    /**
     * Checks if the event has been canceled
     */
    public boolean isCancelled() {
        return this.isCancelled;
    }

    /**
     * Set if the event should be canceled or not
     */
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
