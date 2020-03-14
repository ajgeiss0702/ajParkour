package us.ajg0702.parkour.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import us.ajg0702.parkour.Main;
import us.ajg0702.parkour.Messages;

/**
 * A class for managing all players in the parkour.
 * To get the instance, use Manager.getInstance()
 * @author ajgeiss0702
 *
 */
public class Manager implements Listener {
	
	static Manager instance = null;
	
	List<PkPlayer> plys = new ArrayList<>();
	
	List<PkArea> areas = new ArrayList<>();
	
	Main main;
	
	Messages msgs;

	public Manager(Main pl) {
		instance = this;
		main = pl;
		
		msgs = main.msgs;
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(pl, new Runnable() {
			public void run() {
				reloadPositions();
			}
		}, 5);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {
			public void run() {
				checkActive();
			}
		}, 15*20, 60*20);
	}
	
	/**
	 * Gets the manager instance
	 * @return the instance of manager;
	 */
	public static Manager getInstance() {
		return instance;
	}
	
	
	/**
	 * Gets all (loaded) arenas
	 * @return A {@link java.util.List List} of {@link us.ajg0702.parkour.game.PkArea PkArea}s
	 */
	public List<PkArea> getAreas() {
		return areas;
	}
	
	/**
	 * Reloads all areas, portals, etc
	 */
	public void reloadPositions() {
		main.areaStorage.reload();
		areas = main.areaStorage.getAreas();
	}
	
	/**
	 * Get an area by name.
	 * @param name A String with the name of the area.
	 * @return The found {@link us.ajg0702.parkour.game.PkArea PkArea}. Null if none is found.
	 */
	public PkArea getArea(String name) {
		PkArea f = null;
		for(PkArea a : areas) {
			if(a.getName().equalsIgnoreCase(name)) {
				f = a;
			}
		}
		return f;
	}
	
	/**
	 * Gets a {@link us.ajg0702.game.PkPlayer PkPlayer}.
	 * @param ply The {@link org.bukkit.entity.Player Player} to find.
	 * @return The found {@link us.ajg0702.game.PkPlayer PkPlayer}. Null if none is found.
	 */
	public PkPlayer getPlayer(Player ply) {
		PkPlayer f = null;
		for(PkPlayer p : plys) {
			if(p.ply.equals(ply)) {
				f = p;
			}
		}
		return f;
	}
	
	/**
	 * Gets a list of all players in all areas.
	 * @return A {@link java.util.List List} of {@link us.ajg0702.parkour.game.PkPlayer PkPlayer}s.
	 */
	public List<PkPlayer> getPlayers() {
		return plys;
	}
	
	/**
	 * Checks if a player is in parkour in any area.
	 * @param ply a {@link org.bukkit.entity.Player Player} to check.
	 * @return A boolean telling if the player is in parkour.
	 */
	public boolean inParkour(Player ply) {
		return this.getPlayer(ply) != null;
	}
	
	/**
	 * Gets all players in an area
	 * @param area A {@link us.ajg0702.parkour.game.PkArea PkArea} that the players are in.
	 * @return A {@link java.util.List List} of all {@link us.ajg0702.parkour.game.PkPlayer PkPlayer}s in the area.
	 */
	public List<PkPlayer> getPlayersInArea(PkArea area) {
		List<PkPlayer> f = new ArrayList<>();
		for(PkPlayer p : plys) {
			if(p.area.equals(area)) {
				f.add(p);
			}
		}
		return f;
	}
	
	/**
	 * Gets the total number of players
	 * @return The number of players in parkour in all areas.
	 */
	public int getTotalPlayers() {
		return plys.size();
	}

	
	
	/**
	 * Kick a player from parkour.
	 * @param ply The {@link org.bukkit.entity.Player Player} to kick.
	 * @return A boolean indicating if the player was found and kicked.
	 */
	public boolean kickPlayer(Player ply) {
		PkPlayer f = getPlayer(ply);
		if(f == null) {
			return false;
		} else {
			f.end();
			return true;
		}
	}
	
	/**
	 * Start parkour for a player
	 * @param ply The {@link org.bukkit.entity.Player Player} to start parkour on.
	 * @return The resulting {@link us.ajg0702.parkour.game.PkPlayer PkPlayer}.
	 */
	public PkPlayer startGame(Player ply) {
		return startGame(ply, null);
	}
	
	/**
	 * Start parkour for a player in a certain area.
	 * @param ply The {@link org.bukkit.entity.Player Player} to start parkour on.
	 * @param area The {@link us.ajg0702.parkour.game.PkArea PkArea} to start the parkour in.
	 * @return The resulting {@link us.ajg0702.parkour.game.PkPlayer PkPlayer}.
	 */
	public PkPlayer startGame(Player ply, PkArea area) {
		if(areas.size() <= 0) {
			return null;
		}
		
		if(getPlayer(ply) != null) {
			return null;
		}
		
		String fm = main.getAConfig().getString("area-selection");
		//String fm = "lowest"; // TODO: fix config. apparently the get method doesnt exist
		PkArea s = area;
		if(area == null) {
			if(fm.equalsIgnoreCase("lowest")) {
				HashMap<Object, Double> ac = new HashMap<Object, Double>();
				for(PkArea p : areas) {
					int c = getPlayerCounts(p);
					ac.put(p, (double)c);
				}
				LinkedHashMap<Object, Double> acs = main.sortByValueWithObjectKey(ac);
				s = (PkArea) acs.keySet().toArray()[ac.keySet().size()-1];
			} else {
				Bukkit.getLogger().warning("[ajParkour] area-selection in config was not set correctly! Will only use first parkour area.");
				s = areas.get(0);
			}
		}
		if(s == null) {
			Bukkit.getLogger().warning("[ajParkour] Something went wrong when selecting which area to use! Selecting first one.");
			s = areas.get(0);
		}
		//ply.sendMessage("Starting game in area '" + s.getName()+"'");
		if(ply.getFoodLevel() <= 6) {
			ply.setFoodLevel(7);
		}
		PkPlayer p = new PkPlayer(ply, this, s);
		plys.add(p);
		return p;
	}
	
	/**
	 * Checks that all players are still in parkour. If they are not, they are removed from the list of player in parkour
	 */
	public void checkActive() {
		Iterator<PkPlayer> iter = plys.iterator();

		while (iter.hasNext()) {
		    PkPlayer p = iter.next();

		    if (!p.active) {
		        iter.remove();
		    }
		}
	}
	
	/**
	 * Get the number of people in an area
	 * @param a The {@link us.ajg0702.parkour.game.PkArea PkArea} to check.
	 * @return The number of people in that area.
	 */
	public int getPlayerCounts(PkArea a) {
		int f = 0;
		for(PkPlayer p : plys) {
			if(p.area.equals(a)) {
				f++;
			}
		}
		return f;
	}
	
	public boolean pluginDisabling = false;
	
	/**
	 * Function called when plugin is getting disabled. It kicks all players from the parkour.
	 */
	public void disable() {
		pluginDisabling = true;
		for(PkPlayer p : plys) {
			p.end();
		}
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		if(getPlayer(p) == null) return;
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if(getPlayer(p) == null) return;
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if(getPlayer(p) == null) return;
		if(e.getMaterial().equals(Material.CHEST)) {
			main.selector.openSelector(p);
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		PkPlayer p = getPlayer(e.getPlayer());
		if(p != null) {
			if(!p.teleporting) {
				World w1 = e.getFrom().getWorld();
				World w2 = e.getTo().getWorld();
				if(w1.equals(w2)) {
					if(e.getFrom().distance(e.getTo()) > 10) {
						p.end(msgs.get("fall.force.reasons.teleport"));
					}
				} else {
					p.end(msgs.get("fall.force.reasons.teleport"));
				}
			}
		}
	}
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		PkPlayer p = getPlayer(e.getPlayer());
		if(p != null) {
			if(!p.checkMadeIt()) {
				p.checkFall();
			}
		}
	}
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		kickPlayer(e.getPlayer());
		main.scores.removeCachedPlayer(e.getPlayer());
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		main.scores.getScore(e.getPlayer().getUniqueId(), null);
		main.scores.getTime(e.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		if(p == null) return;
		
		if(getPlayer(p) != null) {
			e.setCancelled(true);
			p.sendMessage(msgs.get("block.place"));
		}
	}
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		if(p == null) return;
		
		if(getPlayer(p) != null) {
			e.setCancelled(true);
			p.sendMessage(msgs.get("block.break"));
		}
	}
	@EventHandler
	public void onHungerDeplete(FoodLevelChangeEvent e) {
		Player p = (Player) e.getEntity();
		if(getPlayer(p) == null) return;
		if(e.getFoodLevel() <= 6) {
			e.setCancelled(true);
			p.setFoodLevel(7);
		}
	}
	
	

}
