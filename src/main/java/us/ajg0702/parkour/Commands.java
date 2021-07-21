package us.ajg0702.parkour;

import java.util.*;

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
import us.ajg0702.parkour.top.TopEntry;
import us.ajg0702.parkour.top.TopManager;
import us.ajg0702.utils.spigot.Config;
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
						resp = msgs.get("errors.notsetup.admin", sply).replaceAll("\\{CMD}", label);
					}
					sender.sendMessage(msgs.get("errors.notsetup.base", sply)+"\n"+resp);
					return true;
				}
				
				if(pl.man.getPlayer(sply) != null) {
					sender.sendMessage(msgs.get("alreadyin", sply));
					return true;
				}
				
				if(sply != null) {
					if(Arrays.asList(config.getString("start-disabled-worlds").split(",")).contains(sply.getWorld().getName())) {
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
						sender.sendMessage(msgs.get("start.started-other").replaceAll("\\{PLY}", pt.getName()));
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
							sender.sendMessage(msgs.get("start.no-area-player", sply).replaceAll("\\{INPUT}", pa));
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
							.replaceAll("\\{PLY}", p.getName())
							.replaceAll("\\{AREA}", a.getName())
						);
				}
				return true;
			case "cachedebug":
				if(!sender.hasPermission("ajparkour.debug")) {
					sender.sendMessage(msgs.get("noperm", sply));
					return true;
				}
				TopManager.getInstance().getHighScores().forEach((key, value) -> {
					StringBuilder sb = new StringBuilder();
					value.forEach((key1, value1) -> sb.append("\n ").append(key1).append(": ").append(value1));
					sender.sendMessage("HS: " + key.getName() + ":" + sb);
				});
				TopManager.getInstance().getLastGetHS().forEach((key, value) -> {
					StringBuilder sb = new StringBuilder();
					value.forEach((key1, value1) -> sb.append("\n ").append(key1).append(": ").append(value1));
					sender.sendMessage("HSLG: " + key.getName() + ":" + sb);
				});

				return true;
			case "areas":
				StringBuilder add1 = new StringBuilder();
				sender.sendMessage(msgs.get("commands.listareas.header", sply));
				for(PkArea a : man.getAreas()) {
					String name = a.getName();
					add1.append(msgs.get("commands.listareas.format", sply)
							.replaceAll("\\{NAME}", name)
							.replaceAll("\\{DIFFICULTY}", a.getDifficulty().toString())).append("\n");
				}
				if(add1.toString().equalsIgnoreCase("")) {
					add1 = new StringBuilder(msgs.get("commands.listareas.none", sply));
				}
				sender.sendMessage(add1.toString());
				return true;
			case "update":
				if(!sender.hasPermission("ajparkour.update")) {
					sender.sendMessage(msgs.get("noperm", sply));
					return true;
				}
				Updater.getInstance().downloadUpdate(sender);
				return true;
			case "blocks":
				if(args.length > 1) {
					if(!sender.hasPermission("ajparkour.selector.openothers")) {
						sender.sendMessage(msgs.get("noperm", sply));
						return true;
					}
					Player tp = Bukkit.getPlayer(args[1]);
					if(tp == null) {
						sender.sendMessage(msgs.get("couldnt-find-player", sply).replaceAll("\\{PLAYER}", args[1]));
						return true;
					}
					pl.selector.openSelector(tp);
					sender.sendMessage(msgs.get("blockselector.openedfor", sply));
					return true;
				} else {
					if(!(sender instanceof Player)) {
						sender.sendMessage(msgs.get("not-from-console"));
						return true;
					}
					if(!sender.hasPermission("ajparkour.selector")) {
						sender.sendMessage(msgs.get("noperm", sply));
						return true;
					}
					pl.selector.openSelector(sply);
				}
				return true;
			case "reset":
				if(sender instanceof Player) {
					sply.sendMessage(msgs.get("not-in-game"));
					return true;
				}
				if(args.length > 1) {
					OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
					scores.setScore(p.getUniqueId(), 0, 0, null);
					sender.sendMessage(msgs.get("reset.success").replaceAll("\\{PLAYER}", p.getName() == null ? p.getUniqueId().toString() : p.getName()));
				} else {
					sender.sendMessage(msgs.get("reset.usage").replaceAll("\\{CMD}", label));
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

				StringBuilder top = new StringBuilder((area == null ?
						msgs.get("top.header", sply) :
						msgs.get("top.header-area", sply).replaceAll("\\{AREA}", area))+"\n");
				boolean doTime = msgs.get("top.format", sply).contains("{TIME}");
				int i;
				for (i = 1; i < config.getInt("top-shown")+1; i++) {
					TopEntry entry = TopManager.getInstance().getTop(i, area);
					if(entry.getName().equalsIgnoreCase("--")) break;
					String replaced = msgs.get("top.format", sply)
							.replaceAll("\\{#}", entry.getPosition()+"")
							.replaceAll("\\{NAME}", entry.getName())
							.replaceAll("\\{SCORE}", entry.getScore()+"") + "\n";
					if(doTime) {
						int time = entry.getTime();
						int min = time / (60);
						int sec = time % (60);
						String timestr = msgs.get("placeholders.stats.time-format", sply)
								.replaceAll("\\{m}", min+"")
								.replaceAll("\\{s}", sec+"");
						if(time < 0) {
							timestr = msgs.get("placeholders.stats.no-data", sply);
						}
						replaced = replaced.replaceAll("\\{TIME}", timestr);
					}
					replaced = replaced.replaceAll("\\{TIME}", msgs.get("placeholders.stats.no-data", sply));

					top.append(replaced);
					if(i != config.getInt("top-shown")) {
						top.append("\n");
					}
				}
				if(i == 1 && TopManager.getInstance().getTop(1, area).getName().equals("--")) {
					sender.sendMessage(msgs.get("nobodys-played-yet", sply));
					return true;
				}
				sender.sendMessage(top.toString());

				return true;
			case "list":
				List<PkPlayer> plys = man.getPlayers();
				StringBuilder add = new StringBuilder(msgs.get("list.header"));
				if(plys.size() <= 0) {
					add.append("\n").append(msgs.get("list.none"));
				}
				for(PkPlayer p : plys) {
					String name = p.getPlayer().getName();
					int score = p.getScore();
					add.append("\n").append(msgs.get("list.format")
							.replaceAll("\\{NAME}", name)
							.replaceAll("\\{SCORE}", score + ""));
				}
				sender.sendMessage(add.toString());
				return true;
			case "removearea":
				if(!sender.hasPermission("ajparkour.setup")) {
					sender.sendMessage(msgs.get("noperm", sply));
					return true;
				}
				if(args.length < 2) {
					sender.sendMessage(msgs.get("areas.remove.help", sply));
					return true;
				}
				if(!pl.areaStorage.config.isSet("areas."+args[1])) {
					sender.sendMessage(msgs.get("areas.remove.cannot-find", sply));
					return true;
				}
				pl.areaStorage.removeArea(args[1]);
				sender.sendMessage(msgs.get("areas.remove.success", sply).replaceAll("\\{NAME}", args[1]));
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
							sender.sendMessage(msgs.get("setup.edit.loaded", sply).replaceAll("\\{CMD}", label));
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
								if(editing.keySet().size() != 0 && editingoverrideplayer != sender) {
									sender.sendMessage(msgs.get("setup.already-creating", sply));
									editingoverrideplayer = (Player) sender;
								} else {
									if(args[2].equalsIgnoreCase("overall")) {
										sender.sendMessage(msgs.get("setup.invalid-name", sply));
										return true;
									}
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
							checkAreaSize(sply, editing);
							return true;
						case "pos2":
							if(editing.keySet().size() == 0) {
								sender.sendMessage(msgs.get("setup.need-to-create", sply));
								return true;
							}
							editing.put("pos2", sply.getLocation());
							sender.sendMessage(msgs.get("setup.set.pos2", sply));
							checkAreaSize(sply, editing);
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
						case "max":
							if(editing.keySet().size() == 0) {
								sender.sendMessage(msgs.get("setup.need-to-create", sply));
								return true;
							}
							if(args.length > 2) {
								String mr = args[2];
								int m;
								if(!isInt(mr)) {
									sender.sendMessage(msgs.get("numberformatexception"));
									return true;
								}
								m = Integer.parseInt(mr);
								editing.put("max", m);
								sender.sendMessage(msgs.get("setup.set.max", sply));
							} else {
								sender.sendMessage(msgs.get("setup.max.need-args", sply));
							}
							return true;
						case "save":
							if(editing.keySet().size() == 0) {
								sender.sendMessage(msgs.get("setup.need-to-create", sply));
								return true;
							}
							if(n(editing.get("pos1")) || n(editing.get("pos2")) || n(editing.get("diff")) || n(editing.get("name"))) {
								sender.sendMessage(msgs.get("setup.save.not-done", sply).replaceAll("\\{CMD}", label));
								return true;
							}
							String name = (String) editing.get("name");
							Location p1 = (Location) editing.get("pos1");
							Location p2 = (Location) editing.get("pos2");
							Location fp = n(editing.get("fallpos")) ? null : (Location) editing.get("fallpos");
							int mp = !(editing.containsKey("max")) ? -1 : (int) editing.get("max");
							Difficulty diff = (Difficulty) editing.get("diff");
							pl.areaStorage.save(new PkArea(name, p1, p2, fp, diff, mp));
							editing = new HashMap<>();
							
							final Player p = (Player) sender;
							Bukkit.getScheduler().runTask(pl, () -> {
								pl.man.reloadPositions();
								p.sendMessage(msgs.get("setup.save.success", p).replaceAll("\\{NAME}", name));
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
							pr.add(msgs.color("&"+(editing.containsKey("max") ? "a" : "7")+"max"));
							pr.add(msgs.color("&"+(editing.containsKey("fallpos") ? "a" : "7")+"fallpos"));
							StringBuilder str = new StringBuilder();
							for(String t : pr) {
								str.append("\n").append(t);
							}
							sender.sendMessage(str.toString());
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
			case "difficulties":
				for(Difficulty d : Difficulty.values()) {
					sender.sendMessage(d.toString()+" ("+d.getMin()+"/"+d.getMax()+")");
				}
				return true;
			case "reload":
				if(!sender.hasPermission("ajparkour.reload")) {
					sender.sendMessage(msgs.get("noperm"));
					return true;
				}
				
				if(args.length >= 2) {
					if(args[1].equalsIgnoreCase("all")) {
						for(String rl : pl.getReloadable()) {
							if(rl.equalsIgnoreCase("all")) continue;
							pl.reload(rl, sender);
						}
					} else {
						pl.reload(args[1].toLowerCase(), sender);
					}
				} else {
					StringBuilder poss = new StringBuilder();
					List<String> possl = pl.getReloadable();
					possl.add("all");
					for(String p : possl) {
						poss.append(p).append(", ");
					}
					poss = new StringBuilder(poss.substring(0, poss.length() - 2));
					sender.sendMessage(msgs.get("reload.usage").replaceAll("\\{CMD}", label).replaceAll("\\{POSS}", poss.toString()));
				}
				
				return true;
			
			default:
				sender.sendMessage(getMainHelp(sply, label));
				return true;
		}
	}

	public void checkAreaSize(Player p, HashMap<String, Object> editing) {
		if(editing.containsKey("pos1") && editing.containsKey("pos2")) {
			Location pos1 = (Location) editing.get("pos1");
			Location pos2 = (Location) editing.get("pos2");

			int x1 = pos1.getBlockX();
			int y1 = pos1.getBlockY();
			int z1 = pos1.getBlockZ();
			int x2 = pos2.getBlockX();
			int y2 = pos2.getBlockY();
			int z2 = pos2.getBlockZ();

			int l = Math.abs(Math.max(x1, x2) - Math.min(x1, x2));
			int h = Math.abs(Math.max(y1, y2) - Math.min(y1, y2));
			int w = Math.abs(Math.max(z1, z2) - Math.min(z1, z2));

			if(l < 20 || h < 20 || w < 20) {
				p.sendMessage(msgs.get("setup.area.too-small")
						.replaceAll("\\{w}", w+"")
						.replaceAll("\\{h}", h+"")
						.replaceAll("\\{l}", l+"")
				);
			}
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
		if(checkPerm("ajparkour.setup", p)) c.add(msgs.get("commands.help.areas", p));
		if(checkPerm("ajparkour.setup", p)) c.add(msgs.get("commands.help.setup", p));
		if(checkPerm("ajparkour.portals", p) || checkPerm("ajparkour.setup", p)) c.add(msgs.get("commands.help.portals", p));
		
		StringBuilder s = new StringBuilder();
		for(String t : c) {
			s.append("\n").append(t);
		}
		return s.toString().replaceAll("\\{CMD}", cmd);
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
		h.add(msgs.get("commands.setup.max", p));
		h.add(msgs.get("commands.setup.info", p));
		h.add(msgs.get("commands.setup.save", p));
		
		StringBuilder s = new StringBuilder();
		for(String t : h) {
			s.append("\n").append(t);
		}
		return s.toString().replaceAll("\\{CMD}", cmd);
	}
	
	private boolean n(Object a) {
		return a == null;
	}
	
	private boolean isInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}

}
