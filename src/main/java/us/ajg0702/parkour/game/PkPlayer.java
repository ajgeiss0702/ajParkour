package us.ajg0702.parkour.game;

import fr.mrmicky.infinitejump.InfiniteJump;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.ajg0702.parkour.Main;
import us.ajg0702.parkour.Messages;
import us.ajg0702.parkour.Rewards;
import us.ajg0702.parkour.Scores;
import us.ajg0702.parkour.api.events.PlayerEndParkourEvent;
import us.ajg0702.parkour.api.events.PlayerJumpEvent;
import us.ajg0702.parkour.api.events.PlayerStartParkourEvent;
import us.ajg0702.parkour.utils.InvManager;
import us.ajg0702.parkour.utils.VersionSupport;
import us.ajg0702.utils.spigot.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PkPlayer implements Listener {
	
	long lastmove;
	
	Player ply;
	Manager man;
	PkArea area;
	
	Messages msgs;
	
	Config config;
	
	Scores scores;
	
	Main plugin;
	
	long started;
	
	List<PkJump> jumps;
	
	int score = 0;
	
	boolean active = true;
	
	boolean teleporting = false;
	
	String block;
	
	int prevhigh = 0;
	
	int afkkick;
	int afktask;
	
	int ahead; // how many (extra) blocks to make ahead
	
	int clearPotsTaskID;
	
	boolean fasterAfkCheck;
	int fastAfkCheckID;
	
	public boolean beatServerHighscore = false;
	
	List<String> cmds = new ArrayList<>();
	
	
	boolean infiniteJump = false;
	InfiniteJump ij;
	boolean ijenableAfter = false;

	/**
	 * Inits a parkour player
	 * @param p The {@link org.bukkit.entity.Player Player} that is in parkour
	 * @param m The {@link us.ajg0702.parkour.game.Manager Manager}.
	 * @param a The {@link us.ajg0702.parkour.game.PkArea PkArea} that the parkour should take place in.
	 */
	public PkPlayer(Player p, Manager m, PkArea a) {
		lastmove = System.currentTimeMillis();
		ply = p;
		man = m;
		area = a;
		plugin = m.main;
		msgs = m.msgs;
		
		ahead = plugin.config.getInt("jumps-ahead");
		
		scores = plugin.scores;
		
		config = plugin.getAConfig();
		
		block = plugin.selector.getBlock(p, area);
		
		
		
		started = System.currentTimeMillis();
		
		afkkick = config.getInt("kick-time");
		
		fasterAfkCheck = config.getBoolean("faster-afk-detection");
		
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			prevhigh = scores.getHighScore(ply.getUniqueId(), config.getBoolean("begin-score-per-area") ? area.getName() : null);
			if(prevhigh > 0 && !(prevhigh+"").equalsIgnoreCase("-1")) {
				p.sendMessage(msgs.get("start.score", p).replaceAll("\\{SCORE}", ""+prevhigh));
			} else {
				p.sendMessage(msgs.get("start.first", p).replaceAll("\\{SCORE}", ""+prevhigh));
			}
		});
		
		
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.scores.addToGamesPlayed(p.getUniqueId()));
		
		
		if(!fasterAfkCheck) {
			if(afkkick > 0) {
				Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
			}
		} else if(afkkick > 0) {
			fastAfkCheckID = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> onMove(new PlayerMoveEvent(ply, p.getLocation(), p.getLocation())), 5, 5).getTaskId();
		}
		
		infiniteJump = Bukkit.getPluginManager().getPlugin("InfiniteJump") != null;
		if(infiniteJump) {
			 ij = (InfiniteJump) Bukkit.getPluginManager().getPlugin("InfiniteJump");
		}
		
		Location start = area.getRandomPosition();
		jumps = new ArrayList<>();
		jumps.add(new PkJump(this, start));
		Location prevJump = jumps.get(jumps.size()-1).getFrom();
		jumps.add(new PkJump(this, prevJump));
		jumps.get(0).place();
		int i = 0;
		while(i < ahead) {
			i++;
			jumps.add(new PkJump(this, jumps.get(jumps.size()-1).getFrom()));
			jumps.get(jumps.size()-1).place();
		}
		
		for(PkJump jump : jumps) {
			if(!jump.isPlaced()) {
				jump.place();
			}
		}
		Location tp = jumps.get(0).getTo();
		teleporting = true;
		p.teleport(new Location(tp.getWorld(), tp.getX()+0.5, tp.getY()+1.5, tp.getZ()+0.5, p.getLocation().getYaw(), p.getLocation().getPitch()));
		Bukkit.getScheduler().scheduleSyncDelayedTask(m.main, () -> teleporting = false, 5);
		
		playSound("start-sound", p);
		
		
		if(config.getBoolean("parkour-inventory")) {
			try {
				InvManager.saveInventory(ply);
				ply.getInventory().clear();
				boolean requirePerm = config.getBoolean("require-permission-for-block-selector-item");
				if(config.getBoolean("enable-block-selector-item") &&
						(!requirePerm || (requirePerm && ply.getPlayer().hasPermission("ajparkour.selector")))) {
					ItemStack bsItem = new ItemStack(Material.CHEST, 1); // bs = block selector
					ItemMeta bsMeta = bsItem.getItemMeta();
					bsMeta.setDisplayName(msgs.get("items.blockselector.name"));
					bsItem.setItemMeta(bsMeta);
					ply.getInventory().setItem(4, bsItem);
				}
				
			} catch (IOException e) {
				ply.sendMessage("&cAn error occured while trying to save your inventory!");
				Bukkit.getLogger().severe("[ajParkour] An error occured while trying to save player's inventory:");
				e.printStackTrace();
			}
		}
		
		if(afkkick >= 0) {
			afktask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
				long distance = System.currentTimeMillis() - lastmove;
				if(distance > (afkkick* 1000L)) {
					end(msgs.get("fall.force.afk"));
				}
			}, afkkick* 20L, 20).getTaskId();
		}
		
		clearPots();
		clearPotsTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			if(Manager.getInstance().getPlayer(ply) != null) {
				clearPots();
			}
		}, 0, 20);
		
		
		
		Bukkit.getPluginManager().callEvent(new PlayerStartParkourEvent(this));
		
		
		if(infiniteJump) {
			if(ij.getJumpManager().isActive(ply)) {
				ij.getJumpManager().disable(ply);
				ijenableAfter = true;
			}
		}
	}
	
	private final List<PotionEffectType> disallowedPots = Arrays.asList(
			PotionEffectType.SPEED,
			PotionEffectType.JUMP,
			PotionEffectType.getByName("LEVITATION"),
			PotionEffectType.getByName("SLOW_FALLING"));
	private void clearPots() {
		for(PotionEffect effect : ply.getActivePotionEffects()) {
	        if(disallowedPots.contains(effect.getType())) {
	        	ply.removePotionEffect(effect.getType());
	        }
	    }
	}
	
	
	private void madeIt() {
		score++;
		jumps.get(0).remove();
		jumps.remove(0);
		Location prevJump = jumps.get(jumps.size()-1).getFrom();
		//ply.sendMessage(AreaStorage.coordsString(prevJump));
		PkJump nj = new PkJump(this, prevJump);
		nj.place();
		jumps.add(nj);
		VersionSupport.sendActionBar(ply, 
				msgs.get("score")
				.replaceAll("\\{SCORE}", score+"")
				.replaceAll("\\{HIGHSCORE}", prevhigh < 0 ? "0" : prevhigh+"")
				);
		
		if(score == prevhigh && prevhigh > 0) {
			ply.sendMessage(msgs.get("beatrecord-ingame", ply).replaceAll("\\{SCORE}", prevhigh+""));
		}
		
		
		int particles = config.getInt("particle-count");
		if(
				!config.getString("new-block-particle").equalsIgnoreCase("none")
				&& particles > 0
				&& VersionSupport.getMinorVersion() >= 9
		) {
			Location njl = nj.getTo();
			ply.spawnParticle(Particle.valueOf(config.getString("new-block-particle")), njl.getBlockX()+0.5, njl.getBlockY()+0.5, njl.getBlockZ()+0.5, particles, 0.005, 0.001, 0.005);
		}
		
		
		playSound("jump-sound", ply, nj.getFrom());
		
		plugin.rewards.checkRewards(this, score, area);
		
		PlayerJumpEvent je = new PlayerJumpEvent(this);
		Bukkit.getPluginManager().callEvent(je);
	}
	
	
	private void playSound(String configkey, Player ply) {
		playSound(configkey, ply, ply.getLocation());
	}
	private void playSound(String configkey, Player ply, Location loc) {
		String soundraw = plugin.getAConfig().getString(configkey);
		if(VersionSupport.getMinorVersion() <= 8 && soundraw.equalsIgnoreCase("ENTITY_CHICKEN_EGG")) {
			soundraw = "CHICKEN_EGG_POP";
		}
		if(soundraw == null) return;
		if(!soundraw.equalsIgnoreCase("none")) {
			Sound sound = null;
			try {
				sound = Sound.valueOf(soundraw);
			} catch(IllegalArgumentException ignored) { }
			if(sound != null) {
				if(VersionSupport.getMinorVersion() >= 12) {
					ply.playSound(loc, sound, SoundCategory.MASTER, 1, 1);
				} else {
					ply.playSound(loc, sound, 1, 1);
				}
			} else {
				Bukkit.getLogger().warning("[ajParkour] Cannot find jump sound '"+soundraw+"'! Make sure it exists on the server version you are running!");
			}
		}
	}
	
	/**
	 * Add a command to execute after the parkour ends.
	 * @param cmd The command to execute in a string.
	 */
	public void addCommand(String cmd) {
		cmds.add(cmd);
	}
	/**
	 * Add a list of commands to execute after the parkour ends.
	 * @param cmds The commands to execute in a string.
	 */
	public void addCommands(List<String> cmds) {
		this.cmds.addAll(cmds);
	}
	
	/**
	 * Gets the highest (y-level) block
	 * @return The PkJump with the highest y-level
	 */
	public PkJump getHighestBlock() {
		PkJump highest = null;
		int y = Integer.MIN_VALUE;
		for(PkJump j : jumps) {
			int jy = j.getTo().getBlockY();
			if(jy > y) {
				y = jy;
				highest = j;
			}
		}
		return highest;
	}
	
	
	/**
	 * Check if the player made the jump
	 * @return A boolean telling if they made the jump or not.
	 */
	public boolean checkMadeIt() {
		double x = ply.getLocation().getX();
		double z = ply.getLocation().getZ();
		
		Location goal = jumps.get(1).getTo();
		double xg = goal.getX()+0.5;
		double zg = goal.getZ()+0.5;
		double xdist = Math.abs(x - xg);
		double ydist = Math.abs(z - zg);
		//ply.sendMessage("x: "+xdist+"\ny: "+ydist);
		if(xdist < 0.8 && ydist < 0.8) {
			madeIt();
			return true;
		}
		return false;
	}
	
	/**
	 * Check if the player fell. If they did, end the parkour
	 */
	public void checkFall() {
		int below = 1;
		Location plyloc = ply.getLocation();
		int my = jumps.get(0).getTo().getBlockY();
		if(
				plyloc.getBlockY() < my-below ||
				ply.isFlying() ||
				plyloc.getBlockY() > getHighestBlock().getTo().getBlockY()+3
			) {
			end();
		}
	}
	
	/**
	 * Get the {@link org.bukkit.entity.Player Player} this instance represents.
	 * @return The {@link org.bukkit.entity.Player Player} this instance represents.
	 */
	public Player getPlayer() {
		return ply;
	}
	/**
	 * Gets the player's current score
	 * @return The score
	 */
	public int getScore() {
		return score;
	}
	
	/**
	 * The material of the blocks. This is selected when the parkour starts.
	 * @return A {@link org.bukkit.Material Material}
	 */
	public String getBlock() {
		return this.block;
	}
	
	
	
	/**
	 * Used to get all the jumps for this player
	 * @return A {@link java.util.List list} of all {@link us.ajg0702.parkour.game.PkJump PkJump}s
	 */
	public List<PkJump> getJumps() {
		return jumps;
	}
	

	
	
	/**
	 * End the parkour with no reason
	 */
	public void end() {
		end("");
	}
	/**
	 * End the parkour with a reason
	 * @param reason The reason to end the parkour
	 */
	public void end(String reason) {
		for(PkJump j : jumps) {
			j.remove();
		}
		
		Bukkit.getScheduler().cancelTask(afktask);
		
		if(!reason.isEmpty()) {
			ply.sendMessage(msgs.get("fall.force.base")+reason);
		}
		ply.sendMessage(msgs.get("fall.normal").replaceAll("\\{SCORE}", score+""));

		Runnable hsTask = () -> {
			int prevscore = scores.getHighScore(ply.getUniqueId(), area.getName());
			if(prevscore < score) {
				int time = (int) (System.currentTimeMillis() - started)/1000;
				scores.setScore(ply.getUniqueId(), score, time, area.getName());
			}
			String scoreArea = plugin.getAConfig().getBoolean("begin-score-per-area") ? area.getName() : null;
			int messageScore = scores.getHighScore(ply.getUniqueId(), scoreArea);
			if(messageScore < score) {
				ply.sendMessage(msgs.get("beatrecord", ply).replaceAll("\\{SCORE}", prevscore+""));
			}
		};
		if(man.pluginDisabling) {
			hsTask.run();
		} else {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, hsTask);
		}
		
		
		
		ply.setFallDistance(Integer.MIN_VALUE);
		
		if(area.getFallPos() != null) {
			teleporting = true;
			ply.teleport(area.getFallPos());
		}
		
		
		if(config.getBoolean("parkour-inventory")) {
			ply.getInventory().clear();
			try {
				InvManager.restoreInventory(ply);
			} catch (IOException e) {
				ply.sendMessage("&cAn error occured while trying to restore your inventory!");
				Bukkit.getLogger().severe("[ajParkour] An error occured while trying to restore player's inventory:");
				e.printStackTrace();
			}
		}
		
		
		
		for(PkJump jump : jumps) {
			jump.remove();
		}
		active = false;
		if(!man.pluginDisabling) {
			man.checkActive();
		}
		
		Bukkit.getScheduler().cancelTask(clearPotsTaskID);
		Bukkit.getScheduler().cancelTask(fastAfkCheckID);
		
		playSound("end-sound", ply);
		
		if(infiniteJump && ijenableAfter) {
			ij.getJumpManager().enable(ply);
		}
		
		if(cmds.size() > 0) {
			Rewards.staticExecuteCommands(cmds, getPlayer());
		}
		
		PlayerEndParkourEvent ee = new PlayerEndParkourEvent(ply, score);
		Bukkit.getPluginManager().callEvent(ee);
	}
	
	
	/**
	 * Get the area that this parkour is in.
	 * @return The {@link us.ajg0702.parkour.game.PkArea PkArea} the parkour is in.
	 */
	public PkArea getArea() {
		return area;
	}
	
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if(!e.getPlayer().equals(ply)) return;
		lastmove = System.currentTimeMillis();
	}

}
