package us.ajg0702.parkour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import us.ajg0702.parkour.game.Difficulty;
import us.ajg0702.parkour.game.Manager;
import us.ajg0702.parkour.game.PkArea;
import us.ajg0702.parkour.game.PkPlayer;
import us.ajg0702.parkour.utils.Config;
import us.ajg0702.parkour.utils.Updater;

public class Commands implements CommandExecutor {
	
	Main pl;
	Messages msgs;
	Manager man;
	Scores scores;
	Config config;

	public Commands(Main plugin) {
		pl = plugin;
		this.man = pl.man;
		pl.getCommand("ajParkour").setExecutor(this);
		msgs = pl.msgs;
		scores = pl.scores;
		config = pl.getAConfig();
	}
	
	HashMap<String, Object> editing = new HashMap<>();
	Player editingoverrideplayer = null;
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player sply = null;
		if(sender instanceof Player) sply = (Player) sender;
		
		if(args.length <= 0) {
			sender.sendMessage(getMainHelp(sply, label));
			return true;
		}
		
		switch(args[0].toLowerCase()) {
			case "start":
				
				
				if(pl.man.getAreas().size() == 0) {
					String resp = msgs.get("errors.notsetup.player", sply);
					if(sender.hasPermission("ajparkour.setup")) {
						resp = msgs.get("errors.notsetup.admin", sply).replaceAll("\\{CMD\\}", label);
					}
					sender.sendMessage(msgs.get("errors.notsetup.base", sply)+"\n"+resp);
					return true;
				}
				
				if(pl.man.getPlayer(sply) != null) {
					sender.sendMessage(msgs.get("alreadyin", sply));
					return true;
				}
				
				if(sply != null) {
					if(config.inCommaList("start-disabled-worlds", sply.getWorld().getName())) {
						sply.sendMessage(msgs.get("disabledworld"));
						return true;
					}
				}
				
				if(args.length <= 1) {
					if(!(sender instanceof Player)) {
						sender.sendMessage(msgs.get("not-from-console"));
						return true;
					}
					pl.man.startGame((Player) sender);
				} else if(args.length == 2) {
					String pa = args[1];
					Player pt = Bukkit.getPlayer(pa);
					if(pt != null) {
						if(!sender.hasPermission("ajparkour.start.others")) {
							sender.sendMessage(msgs.get("noperm", sply));
							return true;
						}
						pl.man.startGame(pt);
						sender.sendMessage(msgs.get("start.started-other").replaceAll("\\{PLY\\}", pt.getName()));
					} else {
						if(!(sender instanceof Player)) {
							sender.sendMessage(msgs.get("not-from-console"));
							return true;
						}
						if(!sender.hasPermission("ajparkour.start.area")) {
							sender.sendMessage(msgs.get("noperm", sply));
							return true;
						}
						PkArea a = pl.man.getArea(pa);
						if(a != null) {
							pl.man.startGame(sply, a);
						} else {
							sender.sendMessage(msgs.get("start.no-area-player", sply).replaceAll("\\{INPUT\\}", pa));
						}
					}
				} else if(args.length == 3) {
					if(
							!(sender.hasPermission("ajparkour.start.others") && sender.hasPermission("ajparkour.start.area"))
							&&
							!sender.hasPermission("ajparkour.start.othersarea")
							) {
						sender.sendMessage(msgs.get("noperm", sply));
						return true;
					}
					Player p = Bukkit.getPlayer(args[1]);
					PkArea a = pl.man.getArea(args[2]);
					if(p == null) {
						sender.sendMessage(msgs.get("start.unknown-player", sply));
						return true;
					}
					if(a == null) {
						sender.sendMessage(msgs.get("start.unknown-area", sply));
						return true;
					}
					pl.man.startGame(p, a);
					sender.sendMessage(
							msgs.get("start.started-other-area", sply)
							.replaceAll("\\{PLY\\}", p.getName())
							.replaceAll("\\{AREA\\}", a.getName())
						);
				}
				return true;
			case "migrate":
				if(!sender.hasPermission("ajparkour.migrate")) {
					sender.sendMessage(msgs.get("noperm", sply));
					return true;
				}
				if(args.length <= 1) {
					sender.sendMessage(msgs.get("migrate.more-args", sply));
					return true;
				}
				int count = pl.scores.migrate(args[1]);
				if(count < 0) {
					sender.sendMessage(msgs.get("migrate.error", sply));
					return true;
				}
				sender.sendMessage(msgs.get("migrate.success", sply).replaceAll("\\{COUNT\\}", count+""));
				return true;
			case "update":
				if(!sender.hasPermission("ajparkour.update")) {
					sender.sendMessage(msgs.get("noperm", sply));
					return true;
				}
				Updater.getInstance().downloadUpdate(sender);
				return true;
			case "blocks":
				if(!(sender instanceof Player)) {
					sender.sendMessage(msgs.get("not-from-console"));
					return true;
				}
				pl.selector.openSelector(sply);
				return true;
			case "reset":
				if(sender instanceof Player) {
					sply.sendMessage(msgs.get("not-in-game"));
					return true;
				}
				if(args.length > 1) {
					OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
					scores.setScore(p.getUniqueId(), 0, 0, null);
					sender.sendMessage(msgs.get("reset.success").replaceAll("\\{PLAYER\\}", p.getName() == null ? p.getUniqueId().toString() : p.getName()));
				} else {
					sender.sendMessage(msgs.get("reset.usage").replaceAll("\\{CMD\\}", label));
				}
				return true;
			case "version":
				sender.sendMessage(msgs.color("&aajParkour &2v&a"+pl.getDescription().getVersion()+" &2by &6ajgeiss0702 &7(https://www.spigotmc.org/members/ajgeiss0702.49935?)"));
				return true;
			case "top":
				
				String area = null;
				if(args.length > 1) {
					area = args[1];
				}
				
				List<UUID> list = scores.getPlayers();
				if(list.size() < 1) {
					sender.sendMessage(msgs.get("nobodys-played-yet", sply));
					return true;
				}
				int maxs = config.getInt("top-shown");
				HashMap<String, Double> map = new HashMap<String, Double>();
				int i = 0;
				for(UUID uuid : list) {
					String name = scores.getName(uuid);
					if(name == null || name.isEmpty() || name.equals("")) {
						name = msgs.color("&7[Unknown]#"+i);
					}
					map.put(name, Double.valueOf(scores.getScore(uuid, area)));
					i++;
				}
				map = pl.sortByValue(map);
				
				i = 1;
				String addList = "";
				if(area == null) {
					addList += msgs.get("top.header", sply)+"\n";
				} else {
					addList += msgs.get("top.header-area", sply).replaceAll("\\{AREA\\}", area)+"\n";
				}
				for( int ai = 0; ai < map.size(); ai++) {
					String key = (String) map.keySet().toArray()[ai];
					addList += msgs.get("top.format", sply)
							.replaceAll("\\{#\\}", i+"")
							.replaceAll("\\{NAME\\}", key.split("#")[0])
							.replaceAll("\\{SCORE\\}", ((int)Math.round(map.get(key)))+"") + "\n";
					i++;
					if(i > maxs) {
						break;
					} else {
						addList += "\n";
					}
				}
				sender.sendMessage(addList);
				return true;
			case "list":
				List<PkPlayer> plys = man.getPlayers();
				String add = msgs.get("list.header");
				if(plys.size() <= 0) {
					add += "\n"+msgs.get("list.none");
				}
				for(PkPlayer p : plys) {
					String name = p.getPlayer().getName();
					int score = p.getScore();
					add += "\n"+msgs.get("list.format")
						.replaceAll("\\{NAME\\}", name)
						.replaceAll("\\{SCORE\\}", score+"");
				}
				sender.sendMessage(add);
				return true;
			case "edit":
				if(!(sender instanceof Player)) {
					sender.sendMessage(msgs.get("not-from-console"));
					return true;
				}
				if(!sply.hasPermission("ajparkour.setup")) {
					sender.sendMessage(msgs.get("noperm", sply));
					return true;
				}
				
				if(args.length >= 2) {
					if(editing.keySet().size() != 0 && editingoverrideplayer != sply) {
						sender.sendMessage(msgs.get("setup.already-creating", sply));
						editingoverrideplayer = (Player) sender;
					} else {
						PkArea a = man.getArea(args[1]);
						if(a != null) {
							editing = new HashMap<>();
							editing.put("name", a.getName());
							editing.put("pos1", a.getPos1());
							editing.put("pos2", a.getPos2());
							editing.put("fallpos", a.getFallPos());
							editing.put("diff", a.getDifficulty());
							sender.sendMessage(msgs.get("setup.edit.loaded", sply).replaceAll("\\{CMD\\}", label));
						} else {
							sender.sendMessage(msgs.get("setup.edit.no-area", sply));
						}
					}
				} else {
					sender.sendMessage(msgs.get("setup.no-name", sply));
				}
				return true;
			case "portals":
				if(!(sender instanceof Player)) {
					sender.sendMessage(msgs.get("not-from-console"));
					return true;
				}
				if(!sply.hasPermission("ajparkour.setup") && !sply.hasPermission("ajparkour.portals")) {
					sender.sendMessage(msgs.get("noperm", sply));
					return true;
				}
				
				pl.areaStorage.setupPortals(sply, args, label);
				return true;
			case "setup":
				if(!(sender instanceof Player)) {
					sender.sendMessage(msgs.get("not-from-console"));
					return true;
				}
				if(!sply.hasPermission("ajparkour.setup")) {
					sender.sendMessage(msgs.get("noperm", sply));
					return true;
				}
				if(args.length > 1) {
					switch(args[1].toLowerCase()) {
						case "create":
							if(args.length >= 3) {
								if(editing.keySet().size() != 0 && editingoverrideplayer != (Player) sender) {
									sender.sendMessage(msgs.get("setup.already-creating", sply));
									editingoverrideplayer = (Player) sender;
								} else {
									editing = new HashMap<>();
									editing.put("name", args[2]);
									sender.sendMessage(msgs.get("setup.created", sply));
								}
							} else {
								sender.sendMessage(msgs.get("setup.no-name", sply));
							}
							return true;
						case "pos1":
							if(editing.keySet().size() == 0) {
								sender.sendMessage(msgs.get("setup.need-to-create", sply));
								return true;
							}
							editing.put("pos1", sply.getLocation());
							sender.sendMessage(msgs.get("setup.set.pos1", sply));
							return true;
						case "pos2":
							if(editing.keySet().size() == 0) {
								sender.sendMessage(msgs.get("setup.need-to-create", sply));
								return true;
							}
							editing.put("pos2", sply.getLocation());
							sender.sendMessage(msgs.get("setup.set.pos2", sply));
							return true;
						case "fallpos":
							if(editing.keySet().size() == 0) {
								sender.sendMessage(msgs.get("setup.need-to-create", sply));
								return true;
							}
							editing.put("fallpos", sply.getLocation());
							sender.sendMessage(msgs.get("setup.set.fallpos", sply));
							return true;
						case "difficulty":
							if(editing.keySet().size() == 0) {
								sender.sendMessage(msgs.get("setup.need-to-create", sply));
								return true;
							}
							if(args.length > 2) {
								String dr = args[2].toUpperCase();
								Difficulty d;
								try {
									d = Difficulty.valueOf(dr);
								} catch(IllegalArgumentException e) {
									sender.sendMessage(msgs.get("setup.diff.not-valid", sply));
									return true;
								}
								editing.put("diff", d);
								sender.sendMessage(msgs.get("setup.set.diff", sply));
							} else {
								sender.sendMessage(msgs.get("setup.diff.need-args", sply));
							}
							return true;
						case "we":
							if(editing.keySet().size() == 0) {
								sender.sendMessage(msgs.get("setup.need-to-create", sply));
								return true;
							}
							Plugin wep = Bukkit.getPluginManager().getPlugin("WorldEdit");
							if(wep == null) {
								sender.sendMessage(msgs.get("setup.we.not-installed", sply));
								return true;
							}
							return WorldeditSelector.bruh((WorldEditPlugin) wep, sender, msgs, editing);
						case "save":
							if(editing.keySet().size() == 0) {
								sender.sendMessage(msgs.get("setup.need-to-create", sply));
								return true;
							}
							if(n(editing.get("pos1")) || n(editing.get("pos2")) || n(editing.get("diff")) || n(editing.get("name"))) {
								sender.sendMessage(msgs.get("setup.save.not-done", sply).replaceAll("\\{CMD\\}", label));
								return true;
							}
							String name = (String) editing.get("name");
							Location p1 = (Location) editing.get("pos1");
							Location p2 = (Location) editing.get("pos2");
							Location fp = n(editing.get("fallpos")) ? null : (Location) editing.get("fallpos");
							Difficulty diff = (Difficulty) editing.get("diff");
							pl.areaStorage.save(new PkArea(name, p1, p2, fp, diff));
							editing = new HashMap<>();
							
							final Player p = (Player) sender;
							Bukkit.getScheduler().runTask(pl, new Runnable() {
								public void run() {
									pl.man.reloadPositions();
									p.sendMessage(msgs.get("setup.save.success", p).replaceAll("\\{NAME\\}", name));
								}
							});
							return true;
						case "info":
							if(editing.keySet().size() == 0) {
								sender.sendMessage(msgs.get("setup.need-to-create", sply));
								return true;
							}
							List<String> pr = new ArrayList<>();
							pr.add(msgs.get("setup.info.header", sply));
							pr.add(msgs.color("&"+(editing.containsKey("pos1") ? "a" : "c")+"pos1"));
							pr.add(msgs.color("&"+(editing.containsKey("pos2") ? "a" : "c")+"pos2"));
							pr.add(msgs.color("&"+(editing.containsKey("diff") ? "a" : "c")+"difficulty"));
							pr.add(msgs.color("&"+(editing.containsKey("fallpos") ? "a" : "7")+"fallpos"));
							String str = "";
							for(String t : pr) {
								str += "\n"+t;
							}
							sender.sendMessage(str);
							return true;
						default:
							if(!sender.hasPermission("ajparkour.setup")) {
								sender.sendMessage(msgs.get("noperm", sply));
								return true;
							}
							sender.sendMessage(getSetupHelp(sply, label));
							return true;
					}
				} else {
					if(!sender.hasPermission("ajparkour.setup")) {
						sender.sendMessage(msgs.get("noperm", sply));
						return true;
					}
					sender.sendMessage(getSetupHelp(sply, label));
					return true;
 				}
				
			case "reload":
				if(!sender.hasPermission("ajparkour.reload")) {
					sender.sendMessage(msgs.get("noperm"));
					return true;
				}
				
				pl.getAConfig().reload();
				pl.areaStorage.reload();
				pl.msgs.reload();
				pl.selector.reloadTypes();
				pl.rewards.reload();
				
				sender.sendMessage(msgs.get("reloaded"));
				return true;
			default:
				sender.sendMessage(getMainHelp(sply, label));
				return true;
		}
	}
	
	public String getMainHelp(Player p, String cmd) {
		List<String> c = new ArrayList<>();
		c.add(msgs.get("commands.help.header", p));
		c.add(msgs.get("commands.help.start", p));
		if(checkPerm("ajparkour.start.area", p)) c.add(msgs.get("commands.help.startarea", p));
		if(checkPerm("ajparkour.start.others", p)) c.add(msgs.get("commands.help.startothers", p));
		c.add(msgs.get("commands.help.list", p));
		c.add(msgs.get("commands.help.top", p));
		if(checkPerm("ajparkour.setup", p)) c.add(msgs.get("commands.help.setup", p));
		if(checkPerm("ajparkour.portals", p) || checkPerm("ajparkour.setup", p)) c.add(msgs.get("commands.help.portals", p));
		
		String s = "";
		for(String t : c) {
			s += "\n"+t;
		}
		return s.replaceAll("\\{CMD\\}", cmd);
	}
	
	private boolean checkPerm(String perm, Player p) {
		if(p == null) {
			return true; // console
		}
		return p.hasPermission(perm);
	}
	
	public String getSetupHelp(Player p, String cmd) {
		List<String> h = new ArrayList<>();
		
		h.add(msgs.get("commands.setup.header", p));
		h.add(msgs.get("commands.setup.edit", p));
		h.add(msgs.get("commands.setup.create", p));
		h.add(msgs.get("commands.setup.pos1", p));
		h.add(msgs.get("commands.setup.pos2", p));
		h.add(msgs.get("commands.setup.fallpos", p));
		h.add(msgs.get("commands.setup.we", p));
		h.add(msgs.get("commands.setup.diff", p));
		h.add(msgs.get("commands.setup.info", p));
		h.add(msgs.get("commands.setup.save", p));
		
		String s = "";
		for(String t : h) {
			s += "\n"+t;
		}
		return s.replaceAll("\\{CMD\\}", cmd);
	}
	
	private boolean n(Object a) {
		return a == null;
	}

}
