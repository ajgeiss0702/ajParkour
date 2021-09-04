package us.ajg0702.parkour.game;

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
import org.bukkit.event.player.*;
import us.ajg0702.parkour.Main;
import us.ajg0702.parkour.Messages;
import us.ajg0702.parkour.api.events.PrePlayerStartParkourEvent;
import us.ajg0702.parkour.top.TopManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

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

		Bukkit.getScheduler().scheduleSyncDelayedTask(pl, this::reloadPositions, 5);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(main, this::checkActive, 15*20, 60*20);
		Bukkit.getScheduler().runTaskTimer(pl, () -> {
			if(main.config.getBoolean("debug")) {
				for(PkArea a : getAreas()) {
					a.draw();
				}
			}
		}, 10, 20);
	}

	/**
	 * Gets the manager instance
	 * @return the instance of the manager
	 */
	public static Manager getInstance() {
		return instance;
	}


	/**
	 * Gets all (loaded) arenas
	 * @return A list of PkAreas
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
	 * Gets a PkPlayer.
	 * @param ply The {@link org.bukkit.entity.Player Player} to find.
	 * @return The found PkPlayer. Null if none is found.
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
	 */
	public void startGame(Player ply) {
		startGame(ply, null);
	}

	/**
	 * Start parkour for a player in a certain area.
	 * @param ply The {@link org.bukkit.entity.Player Player} to start parkour on.
	 * @param area The {@link us.ajg0702.parkour.game.PkArea PkArea} to start the parkour in.
	 */
	public void startGame(Player ply, PkArea area) {
		if(areas.size() <= 0) {
			return;
		}

		if(getPlayer(ply) != null) {
			return;
		}

		String fm = main.getAConfig().getString("area-selection");
		PkArea s = area;
		if(area == null) {
			if(fm.equalsIgnoreCase("lowest")) {
				HashMap<Object, Double> ac = new HashMap<>();
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
		if(getPlayerCounts(s) >= s.getMax()) {
			ply.sendMessage(msgs.get("areafull"));
			return;
		}
		//ply.sendMessage("Starting game in area '" + s.getName()+"'");
		if(ply.getFoodLevel() <= 6) {
			ply.setFoodLevel(7);
		}

		PrePlayerStartParkourEvent preevent = new PrePlayerStartParkourEvent(ply);
		Bukkit.getPluginManager().callEvent(preevent);
		if(preevent.isCancelled()) {
			return;
		}

		PkPlayer p = new PkPlayer(ply, Manager.getInstance(), s);
		plys.add(p);
	}

	/**
	 * Checks that all players are still in parkour. If they are not, they are removed from the list of player in parkour
	 */
	public void checkActive() {
		plys.removeIf(p -> !p.active);
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
		main.getLogger().info("Removing all active players from parkour because the plugin is disabling.");
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
		if(e.getMaterial().equals(Material.CHEST) && main.config.getBoolean("parkour-inventory")) {
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
		if(p == null) return;
		if(!p.checkMadeIt()) p.checkFall();
	}
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		kickPlayer(e.getPlayer());
		TopManager.getInstance().clearPlayerCache(e.getPlayer());
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		if(p == null) return;

		if(getPlayer(p) != null) {
			e.setCancelled(true);
			p.sendMessage(msgs.get("block.place", p));
		}
	}
	@EventHandler
	public void onEmptyBucket(PlayerBucketEmptyEvent e) {
		Player p = e.getPlayer();
		if(p == null) return;

		if(getPlayer(p) != null) {
			e.setCancelled(true);
			p.sendMessage(msgs.get("block.place", p));
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

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		main.scores.updateName(e.getPlayer());
	}



}
