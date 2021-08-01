package us.ajg0702.parkour;

import java.io.File;
import java.io.IOException;
import java.util.*;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.ajg0702.parkour.api.events.PlayerEndParkourEvent;
import us.ajg0702.parkour.api.events.PlayerStartParkourEvent;
import us.ajg0702.parkour.game.PkArea;
import us.ajg0702.parkour.game.PkPlayer;
import us.ajg0702.parkour.top.TopManager;

public class Rewards implements Listener {

	Main plugin;
	
	YamlConfiguration rw;
	File rwFile;
	
	Messages msgs;
	
	Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
	
	public Rewards(Main plugin) {
		this.plugin = plugin;
		msgs = plugin.msgs;

		Bukkit.getPluginManager().registerEvents(this, plugin);

		rwFile = new File(plugin.getDataFolder(), "rewards.yml");
		reload();
	}
	
	public void reload() {
		rw = YamlConfiguration.loadConfiguration(rwFile);
		rw.options().header("This is the rewards file.\nHere is a guide for this: https://wiki.ajg0702.us/ajparkour/configs/Rewards");
		YamlConfiguration oldconfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
		if(!rw.isSet("intervals")) {
			if(oldconfig.isSet("rewards.interval")) {
				ConfigurationSection r = oldconfig.getConfigurationSection("rewards");
				int interval = r.getInt("interval");
				String message = r.getString("message");
				List<String> commands;
				if(r.isString("command")) {
					commands = Collections.singletonList(r.getString("command"));
				} else {
					commands = r.getStringList("command");
				}
				rw.set("intervals."+interval+".message", message);
				rw.set("intervals."+interval+".commands", commands);
			} else {
				rw.set("intervals.10.message", "&aCongrats! &7You got to {SCORE} jumps! &bHave a diamond!");
				rw.set("intervals.10.commands", Collections.singletonList("give {PLAYER} diamond 1"));
			}
		}
		if(!rw.isSet("exceptions")) {
			if(oldconfig.isSet("rewards.exceptions")) {
				ConfigurationSection s = oldconfig.getConfigurationSection("rewards.exceptions");
				for(String k : s.getKeys(false)) {
					ConfigurationSection ss = s.getConfigurationSection(k);
					if(ss.isString("command")) {
						s.set(k+".commands", Collections.singletonList(ss.getString("command")));
					} else {
						s.set(k+".commands", ss.getStringList("command"));
					}
					s.set(k+".command", null);
				}
				rw.set("exceptions", s);
			} else {
				rw.set("exceptions.1.message", "&a&lCongrats, you made a jump!");
				rw.set("exceptions.1.first-time-only", true);
			}
		}
		if(!rw.isSet("specials")) {
			rw.set("specials.beat-server-record.message", "&a&lCongrats!&r&7 You reached the server high score!");
			rw.set("specials.beat-server-record.commands", Collections.singletonList("give {PLAYER} emerald 1"));
		}
		if(!rw.isSet("specials.start")) {
			rw.set("specials.start.message", "");
			rw.set("specials.start.commands", Collections.emptyList());
		}
		if(!rw.isSet("specials.end")) {
			rw.set("specials.end.message", "");
			rw.set("specials.end.commands", Collections.emptyList());
		}
		save();
	}
	
	private void save() {
		try {
			rw.save(rwFile);
		} catch (IOException e) {
			Bukkit.getLogger().severe("Unable to save rewards file: ");
			e.printStackTrace();
		}
	}
	
	
	public void checkRewards(PkPlayer p, final int score, PkArea area) {
		
		final Player player = p.getPlayer();
		
		if(!p.beatServerHighscore) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				int topscore;
				try {
					int number = 1;
					topscore = TopManager.getInstance().getTop(1, null).getScore();

				} catch(Exception e) {return;}
				if(score > topscore) {
					String message = msgs.color(rw.getString("specials.beat-server-record.message", ""));
					if(!message.isEmpty()) {
						player.sendMessage(message);
					}
					List<String> cmds = rw.getStringList("specials.beat-server-record.commands"	);
					cmds.replaceAll(s -> s.replaceAll("\\{SCORE}", score+""));
					if(cmds.size() != 0) {
						Bukkit.getScheduler().runTask(plugin, () -> executeCommands(cmds, p));
					}
					p.beatServerHighscore = true;
				}
			});
		}
		
		
		
		List<String> es = getExceptions();
		for(String ec : es) {
			//p.getPlayer().sendMessage(ec+"");
			int n = Integer.decode(ec.split("-")[0]);
			if(n == score) {
				//p.getPlayer().sendMessage(ec+" good!");
				boolean firstTimeOnly = rw.getBoolean("exceptions."+ec+".first-time-only", false);
				//p.getPlayer().sendMessage(ec+".first-time-only: "+firstTimeOnly);
				if(firstTimeOnly) {
					int highscore = plugin.scores.getHighScore(p.getPlayer().getUniqueId(), null);
					if(n <= highscore) {
						//p.getPlayer().sendMessage(ec+" <= "+highscore);
						continue;
					}
				}
				
				String areas = rw.getString("exceptions."+ec+".areas", "");
				boolean skip = false;
				if(!areas.isEmpty()) {
					String[] parts = areas.split(",");
					for(String a : parts) {
						//p.getPlayer().sendMessage("Checking area "+a);
						boolean not = a.indexOf("!") == 0;
						//p.getPlayer().sendMessage("- not: "+not);
						if(not) a = a.substring(1);
						//p.getPlayer().sendMessage("- After substring: "+a);
						PkArea ar = plugin.man.getArea(a);
						if(ar != null) {
							if(not) {
								//p.getPlayer().sendMessage("- area not");
								if(ar.getName().equals(area.getName())) {
									skip = true;
								}
							} else {
								//p.getPlayer().sendMessage("- area must");
								if(!ar.equals(area)) {
									skip = true;
								}
							}
						}
					}
				}
				if(skip) continue;
				
				
				String permission = rw.getString("exceptions."+ec+".permission", "");
				if(!permission.isEmpty()) {
					boolean notperm = permission.indexOf('!') == 0;
					if(notperm) {
						permission = permission.substring(1);
						if(p.getPlayer().hasPermission(permission)) continue;
					} else {
						if(!p.getPlayer().hasPermission(permission)) continue;
					}
				}
				
				
				int cooldown = rw.getInt("exceptions."+ec+".cooldown", 0);
				if(cooldowns.containsKey(p.getPlayer().getUniqueId())) {
					Map<String, Long> cds = cooldowns.get(p.getPlayer().getUniqueId());
					if(cds.containsKey(ec)) {
						long last = cds.get(ec);
						long now = System.currentTimeMillis();
						if((now-last)/1000 < cooldown) {
							//p.getPlayer().sendMessage("cooldown caught it!");
							continue;
						} else {
							cds.put(ec, System.currentTimeMillis());
						}
					} else {
						cds.put(ec, System.currentTimeMillis());
					}
				} else {
					cooldowns.put(p.getPlayer().getUniqueId(), new HashMap<>());
				}
				
				
				String message = rw.getString("exceptions."+ec+".message", "");
				if(!message.isEmpty()) {
					p.getPlayer().sendMessage(msgs.color(message.replaceAll("\\{SCORE}", score+"")));
				}

				List<String> cmds = rw.getStringList("exceptions."+ec+".commands");
				cmds.replaceAll(s -> s.replaceAll("\\{SCORE}", score+""));
				executeCommands(cmds, p);
				break;
			}
		}
		
		List<String> is = getIntervals();
		for(String ic : is) {
			int n = Integer.decode(ic.split("-")[0]);
			if(n <= 0) continue;
			if(score % n == 0) {
				boolean skip = false;
				
				String areas = rw.getString("intervals."+ic+".areas", "");
				if(!areas.isEmpty()) {
					String[] parts = areas.split(",");
					for(String a : parts) {
						//p.getPlayer().sendMessage("Checking area "+a);
						boolean not = a.indexOf("!") == 0;
						//p.getPlayer().sendMessage("- not: "+not);
						if(not) a = a.substring(1);
						//p.getPlayer().sendMessage("- After substring: "+a);
						PkArea ar = plugin.man.getArea(a);
						if(ar != null) {
							if(not) {
								//p.getPlayer().sendMessage("- area not");
								if(ar.getName().equals(area.getName())) {
									skip = true;
								}
							} else {
								//p.getPlayer().sendMessage("- area must");
								if(!ar.equals(area)) {
									skip = true;
								}
							}
						}
					}
				}
				
				if(skip) continue;
				
				
				String permission = rw.getString("intervals."+ic+".permission", "");
				if(!permission.isEmpty()) {
					boolean notperm = permission.indexOf('!') == 0;
					if(notperm) {
						permission = permission.substring(1);
						if(p.getPlayer().hasPermission(permission)) continue;
					} else {
						if(!p.getPlayer().hasPermission(permission)) continue;
					}
				}
				
				String message = rw.getString("intervals."+ic+".message", "").replaceAll("\\{SCORE}", score+"");
				if(!message.isEmpty()) {
					p.getPlayer().sendMessage(msgs.color(message));
				}

				List<String> cmds = rw.getStringList("intervals."+ic+".commands");
				cmds.replaceAll(s -> s.replaceAll("\\{SCORE}", score+""));
				executeCommands(cmds, p);
			}
		}
		
		//p.getPlayer().sendMessage("hello "+es.size()+" "+is.size());
	}
	
	public void executeCommands(List<String> cmds, PkPlayer p) {
		executeCommands(cmds, p, false);
	}
	
	public void executeCommands(List<String> cmds, PkPlayer p, boolean dontSkip) {
		//Bukkit.getLogger().info("First execute commands");
		if(dontSkip) {
			//Bukkit.getLogger().info("Going straight to executing commands");
			staticExecuteCommands(cmds, p.getPlayer());
			return;
		}
		if(!plugin.getAConfig().getString("execute-reward-commands").equalsIgnoreCase("earned")) {
			//Bukkit.getLogger().info("Adding commands to list");
			p.addCommands(cmds);
			return;
		}
		staticExecuteCommands(cmds, p.getPlayer());
	}

	@EventHandler
	public void onStart(PlayerStartParkourEvent e) {
		PkPlayer p = e.getParkourPlayer();
		String m = rw.getString("specials.start.message");
		if(!m.isEmpty()) {
			if(plugin.papi) {
				m = PlaceholderAPI.setPlaceholders(p.getPlayer(), m);
			}
			p.getPlayer().sendMessage(m);
		}
		staticExecuteCommands(rw.getStringList("specials.start.commands"), p.getPlayer());
	}

	@EventHandler
	public void onFall(PlayerEndParkourEvent e) {
		Player p = e.getPlayer();
		String m = rw.getString("specials.end.message").replaceAll("\\{SCORE}", e.getFallScore()+"");
		if(!m.isEmpty()) {
			if(plugin.papi) {
				m = PlaceholderAPI.setPlaceholders(p, m);
			}
			p.getPlayer().sendMessage(m);
		}
		List<String> commands = rw.getStringList("specials.end.commands");
		commands.replaceAll(s -> s.replaceAll("\\{SCORE}", e.getFallScore()+""));
		staticExecuteCommands(commands, p);
	}
	
	public static void staticExecuteCommands(List<String> cmds, Player p) {
		//Bukkit.getLogger().info("staticExecuteRewards is getting executed");
		for(String cmdr : cmds) {
			//Bukkit.getLogger().info("staticExecuteRewards: "+cmdr);
			if(cmdr == null) continue;
			boolean execAsPlayer = (cmdr.indexOf("[p]") == 0);
			String cmd = (execAsPlayer) ? cmdr.replaceFirst("\\[p]", "") : cmdr;
			if(cmd.indexOf(" ") == 0) {
				cmd = cmd.replaceFirst(" ", "");
			}
			if(cmd.indexOf("/") == 0) {
				cmd = cmd.replaceFirst("/", "");
			}
			
			cmd = cmd.replaceAll("\\{PLAYER}", p.getPlayer().getName());
			
			if(execAsPlayer) {
				Bukkit.dispatchCommand(p.getPlayer(), cmd);
			} else {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
			}
		}
	}
	
	
	private List<String> getIntervals() {
		Set<String> k = rw.getConfigurationSection("intervals").getKeys(false);
		return new ArrayList<>(k);
	}
	private List<String> getExceptions() {
		Set<String> k = rw.getConfigurationSection("exceptions").getKeys(false);
		return new ArrayList<>(k);
	}

}
