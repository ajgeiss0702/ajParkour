package us.ajg0702.parkour.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.ajg0702.parkour.Main;
import us.ajg0702.parkour.Messages;
import us.ajg0702.parkour.Rewards;
import us.ajg0702.parkour.Scores;
import us.ajg0702.parkour.game.Manager;
import us.ajg0702.parkour.utils.Config;
import us.ajg0702.parkour.utils.InvManager;
import us.ajg0702.parkour.utils.VersionSupport;

public class PkPlayer implements Listener {
	
	long lastmove = System.currentTimeMillis();
	
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
	
	int lasty = 0;
	
	int ahead = 1; // how many (extra) blocks to make ahead
	
	List<String> cmds = new ArrayList<>();

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
		
		prevhigh = scores.getScore(ply.getUniqueId(), config.getBoolean("begin-score-per-area") ? area.getName() : null);
		
		started = System.currentTimeMillis();
		
		afkkick = config.getInt("kick-time");
		
		
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		
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
		Bukkit.getScheduler().scheduleSyncDelayedTask(m.main, new Runnable() {
			public void run() {
				teleporting = false;
			}
		}, 5);
		if(prevhigh > 0 && !(prevhigh+"").equalsIgnoreCase("-1")) {
			p.sendMessage(msgs.get("start.score", p).replaceAll("\\{SCORE\\}", ""+prevhigh));
		} else {
			p.sendMessage(msgs.get("start.first", p).replaceAll("\\{SCORE\\}", ""+prevhigh));
		}
		playSound("start-sound", p);
		
		
		if(config.getBoolean("parkour-inventory")) {
			try {
				InvManager.saveInventory(ply);
				ply.getInventory().clear();
				ItemStack bsItem = new ItemStack(Material.CHEST, 1); // bs = block selector
				ItemMeta bsMeta = bsItem.getItemMeta();
				bsMeta.setDisplayName(msgs.get("items.blockselector.name"));
				bsItem.setItemMeta(bsMeta);
				ply.getInventory().setItem(4, bsItem);
			} catch (IOException e) {
				ply.sendMessage("&cAn error occured while trying to save your inventory!");
				Bukkit.getLogger().severe("[ajParkour] An error occured while trying to save player's inventory:");
				e.printStackTrace();
			}
		}
		
		if(afkkick >= 0) {
			afktask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
				public void run() {
					long distance = System.currentTimeMillis() - lastmove;
					if(distance > (afkkick*1000)) {
						end(msgs.get("fall.force.afk"));
					}
				}
			}, afkkick*20, 20).getTaskId();
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
		VersionSupport.sendActionBar(ply, msgs.get("score").replaceAll("\\{SCORE\\}", score+""));
		
		if(score == prevhigh && prevhigh > 0) {
			ply.sendMessage(msgs.get("beatrecord-ingame", ply).replaceAll("\\{SCORE\\}", prevhigh+""));
		}
		
		
		int particles = config.getInt("particle-count");
		if(particles > 0 && VersionSupport.getMinorVersion() >= 9) {
			Location njl = nj.getTo();
			ply.spawnParticle(Particle.CLOUD, njl.getBlockX()+0.5, njl.getBlockY()+0.5, njl.getBlockZ()+0.5, particles, 0.005, 0.001, 0.005);
		}
		
		
		playSound("jump-sound", ply, nj.getFrom());
		
		plugin.rewards.checkRewards(this, score, area);
	}
	
	
	private void playSound(String configkey, Player ply) {
		playSound(configkey, ply, ply.getLocation());
	}
	private void playSound(String configkey, Player ply, Location loc) {
		String soundraw = plugin.getAConfig().getString(configkey);
		if(soundraw == null) return;
		if(!soundraw.equalsIgnoreCase("none")) {
			Sound sound = null;
			try {
				sound = Sound.valueOf(soundraw);
			} catch(IllegalArgumentException e) { }
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
	 * Check if the player made the jump
	 * @return A boolean telling if they made the jump or not.
	 */
	public boolean checkMadeIt() {
		int x = ply.getLocation().getBlockX();
		int z = ply.getLocation().getBlockZ();
		
		Location goal = jumps.get(1).getTo();
		int xg = goal.getBlockX();
		int zg = goal.getBlockZ();
		if(x == xg && z == zg) {
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
		int my = jumps.get(0).getTo().getBlockY();
		if(ply.getLocation().getBlockY() < my-below || ply.isFlying()) {
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
		ply.sendMessage(msgs.get("fall.normal").replaceAll("\\{SCORE\\}", score+""));
		
		int prevscore = scores.getScore(ply.getUniqueId(), area.getName());
		if(prevscore < score) {
			int time = (int) (System.currentTimeMillis() - started)/1000;
			scores.setScore(ply.getUniqueId(), score, time, area.getName());
			ply.sendMessage(msgs.get("beatrecord", ply).replaceAll("\\{SCORE\\}", prevscore+""));
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
		
		
		
		if(cmds.size() > 0) {
			Rewards.staticExecuteCommands(cmds, this);
		}
		
		for(PkJump jump : jumps) {
			jump.remove();
		}
		active = false;
		if(!man.pluginDisabling) {
			man.checkActive();
		}
		
		playSound("end-sound", ply);
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
