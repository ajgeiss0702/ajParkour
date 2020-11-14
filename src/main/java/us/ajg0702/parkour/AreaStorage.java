package us.ajg0702.parkour;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import us.ajg0702.parkour.game.Difficulty;
import us.ajg0702.parkour.game.Manager;
import us.ajg0702.parkour.game.PkArea;
import us.ajg0702.utils.spigot.Config;

public class AreaStorage implements Listener {

	Main plugin;
	
	File configfile;
	YamlConfiguration config;
	
	Config mainConfig;
	
	Messages msgs;
	
	
	public AreaStorage(Main plugin) {
		this.plugin = plugin;
		
		msgs = plugin.msgs;
		
		mainConfig = plugin.getAConfig();
		
		configfile = new File(plugin.getDataFolder(), "positions.yml");
		config = YamlConfiguration.loadConfiguration(configfile);
		
		
		if(mainConfig.getBoolean("enable-portals") && !mainConfig.getBoolean("faster-portals")) {
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
		
		if(mainConfig.getBoolean("enable-portals") && mainConfig.getBoolean("faster-portals")) {
			Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
				public void run() {
					for(Player p : Bukkit.getOnlinePlayers()) {
						checkPortal(new PlayerMoveEvent(p, p.getLocation(), p.getLocation()));
					}
				}
			}, 3*20, 5);
		}
		
		
		saveFile();
	}
	
	public void save(PkArea area) {
		HashMap<String, Object> d = new HashMap<>();
		d = add(d, "pos1", area.getPos1());
		d = add(d, "pos2", area.getPos2());
		d = add(d, "fallpos", area.getFallPos());
		d = add(d, "difficulty", area.getDifficulty().toString());
		d = add(d, "max", area.getMax());
		config.set("areas."+area.getName(), d);
		saveFile();
	}
	
	public void reload() {
		configfile = new File(plugin.getDataFolder(), "positions.yml");
		config = YamlConfiguration.loadConfiguration(configfile);
	}
	
	public List<PkArea> getAreas() {
		if(!config.isSet("areas")) {
			return new ArrayList<>();
		}
		ConfigurationSection s = (ConfigurationSection) config.get("areas");
		if(s == null) {
			return new ArrayList<>();
		}
		List<PkArea> list = new ArrayList<>();
		for(String name : s.getKeys(false)) {
			Location p1 = parseLoc(s.getString(name+".pos1"));
			Location p2 = parseLoc(s.getString(name+".pos2"));
			Location fp = parseLoc(s.getString(name+".fallpos"));
			String diffraw = s.getString(name+".difficulty");
			int maxplayers = s.getInt(name+".max", -1);
			if(diffraw == null) {
				diffraw = "BALANCED";
				Bukkit.getLogger().warning("[ajParkour] Could not load difficulty for area '"+name+"'! Defaulting to BALANCED. Please edit the area and set the difficulty!");
			}
			Difficulty diff = Difficulty.valueOf(diffraw);
			if(diff == null) {
				diff = Difficulty.BALANCED;
				Bukkit.getLogger().warning("[ajParkour] Could not load difficulty for area '"+name+"'! Defaulting to BALANCED. Please edit the area and fix the difficulty!");
			}
			//System.out.println(name+", "+nstring(p1)+", "+nstring(p2)+", "+nstring(fp));
			list.add(new PkArea(name, p1, p2, fp, diff, maxplayers));
		}
		return list;
	}
	
	
	public void save(Portal portal) {
		HashMap<String, Object> d = new HashMap<>();
		d = add(d, "loc", portal.getLoc());
		
		PkArea ar = portal.getArea();
		if(ar != null) {
			d = add(d, "area", ar.getName());
		}
	
		config.set("portals."+portal.getName(), d);
		saveFile();
	}
	
	public List<Portal> getPortals() {
		if(!config.isSet("portals")) {
			YamlConfiguration oldconfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
			if(oldconfig.isSet("area.portals")) {
				@SuppressWarnings("unchecked")
				List<HashMap<String, Object>> oldportals = (List<HashMap<String, Object>>) oldconfig.getList("area.portals");
				int i = 0;
				for(HashMap<String, Object> p : oldportals) {
					i++;
					World world = Bukkit.getWorld(p.get("world").toString());
					if(world == null) {
						Bukkit.getLogger().warning("[ajParkour] Could not convert portal: Unknown world '" + p.get("world").toString()+"'!");
					}
					int x = Integer.valueOf(p.get("x").toString());
					int y = Integer.valueOf(p.get("y").toString());
					int z = Integer.valueOf(p.get("z").toString());
					Location ploc = new Location(world, x, y, z);
					save(new Portal(i+"", ploc, null));
				}
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						reload();
					}
 				}, 20);
			} else {
				return new ArrayList<>();
			}
		}
		ConfigurationSection s = (ConfigurationSection) config.get("portals");
		if(s == null) {
			return new ArrayList<>();
		}
		List<Portal> list = new ArrayList<>();
		for(String name : s.getKeys(false)) {
			//Bukkit.getLogger().info("[ajParkour] Portal: "+name);
			Location loc = parseLoc(s.getString(name+".loc"));
			String arearaw = s.getString(name+".area");
			
			PkArea area = null;
			if(arearaw != null) {
				PkArea t = plugin.man.getArea(arearaw);
				if(t != null) {
					area = t;
				} else {
					Bukkit.getLogger().warning("[ajParkour] Unable to load area for portal '"+name+"'! Did the area get deleted?");
				}
			}

			//Bukkit.getLogger().info("[ajParkour] adding portal "+name+" to list");
			list.add(new Portal(name, loc, area));
		}
		return list;
	}
	
	@SuppressWarnings("unused")
	private String nstring(Object raw) {
		if(raw == null) {
			return "null";
		}
		return raw+"";
	}
	
	
	public Location parseLoc(String raw) {
		if(raw == null) return null;
		String[] a = raw.split(",");
		if(a.length == 6) {
			return new Location(Bukkit.getWorld(a[0]), Double.valueOf(a[1]), Double.valueOf(a[2]), Double.valueOf(a[3]), Float.valueOf(a[4]), Float.valueOf(a[5]));
		}
		else if(a.length == 4) {
			return new Location(Bukkit.getWorld(a[0]), Double.valueOf(a[1]), Double.valueOf(a[2]), Double.valueOf(a[3]));
		} else {
			throw new IllegalArgumentException("Excepted 4 or 6 numbers but got " + a.length);
		}
	}
	
	private void saveFile() {
		config.options().header(" This is where the plugin stores all the areas and portals.\n "
				+ "Be careful when editing this file, it could cause some areas or portals to fail to load!");
		config.options().copyHeader(true);
		try {
			config.save(configfile);
		} catch (IOException e) {
			Bukkit.getLogger().severe("[ajParkour] Failed to save areas config:");
			e.printStackTrace();
		}
	}
	
	
	private HashMap<String, Object> add(HashMap<String, Object> before, String key, Object add) {
		if(add instanceof Location) {
			Location l = (Location) add;
			String world = l.getWorld().getName();
			int x = l.getBlockX();
			int y = l.getBlockY();
			int z = l.getBlockZ();
			float p = l.getPitch();
			float yaw = l.getYaw();
			before.put(key, world+","+x+","+y+","+z+","+yaw+","+p);
		} else {
			before.put(key, add);
		}
		
		return before; 
	}
	
	
	public void setupPortals(Player p, String[] args, String label) {
		
		if(args.length == 1) {
			sendPortalHelp(p, label);
			return;
		}
		switch(args[1]) {
		case "list":
			String f = msgs.get("portals.list.header", p)+"\n";
			for(Portal po : getPortals()) {
				String name = po.getName();
				String coords = coordsString(po.getLoc());
				PkArea area = po.getArea();
				if(area != null && !area.getName().isEmpty()) {
					f += msgs.get("portals.list.format.area", p)
							.replaceAll("\\{NAME\\}", name)
							.replaceAll("\\{COORDS\\}", coords)
							.replaceAll("\\{AREA\\}", area.getName())+"\n";
				} else {
					f += msgs.get("portals.list.format.no-area", p)
							.replaceAll("\\{NAME\\}", name)
							.replaceAll("\\{COORDS\\}", coords)+"\n";
				}
			}
			p.sendMessage(f);
			break;
		case "create":
			if(args.length < 3) {
				p.sendMessage(msgs.get("portals.create.help", p).replaceAll("\\{CMD\\}", label));
				return;
			}
			String name = args[2];
			PkArea area = null;
			if(args.length >= 4) {
				String areaname = args[3];
				area = plugin.man.getArea(areaname);
			}
			Location loc = p.getLocation();
			Portal newp = new Portal(name, loc, area);
			save(newp);
			reload();
			Manager.getInstance().reloadPositions();
			p.sendMessage(msgs.get("portals.create.success", p).replaceAll("\\{NAME\\}", name));
			
			break;
		case "remove":
			if(args.length < 3) {
				p.sendMessage(msgs.get("portals.remove.help", p));
				break;
			}
			String portalname = args[2];
			Set<String> portals = config.getConfigurationSection("portals").getKeys(false);
			if(portals.contains(portalname)) {
				config.set("portals."+portalname, null);
				p.sendMessage(msgs.get("portals.remove.success", p).replaceAll("\\{NAME\\}", portalname));
				saveFile();
				Manager.getInstance().reloadPositions();
			} else {
				p.sendMessage(msgs.get("portals.remove.cannot-find", p));
			}
			break;
		default:
			sendPortalHelp(p, label);
			break;
		}
		
	}
	
	private void sendPortalHelp(Player p, String label) {
		List<String> h = new ArrayList<>();
		
		h.add(msgs.get("commands.portals.header", p));
		h.add(msgs.get("commands.portals.list", p));
		h.add(msgs.get("commands.portals.create", p));
		h.add(msgs.get("commands.portals.remove", p));

		
		String s = "";
		for(String t : h) {
			s += "\n"+t;
		}
		p.sendMessage(s.replaceAll("\\{CMD\\}", label));
	}
	
	
	public void removeArea(String area) {
		if(!config.isSet("areas."+area)) {
			return;
		}
		config.set("areas."+area, null);
		saveFile();
		reload();
		Manager.getInstance().reloadPositions();
	}
	
	
	@EventHandler
	public void checkPortal(PlayerMoveEvent e) {
		
		/*float yaw = e.getPlayer().getLocation().getYaw(); // this is me messing with converting from (0 to 360) to (0 to 180 & -180 to 0)
		float oyaw = Float.valueOf(yaw);
		
		
		if(yaw < 0) {
			yaw +=360;
		}
		
		if(yaw > 180) {
			yaw -= 360;
			yaw = Math.abs(yaw)*-1;
		}
		
		e.getPlayer().sendMessage(yaw+" o: "+oyaw);*/
		
		if(Manager.getInstance().inParkour(e.getPlayer())) {
			return;
		}
		
		
		Location l = e.getTo();
		List<Portal> portals = getPortals();
		for(Portal p : portals) {
			//e.getPlayer().sendMessage("Portal: "+p.getName());
			Location loc = p.getLoc();
			PkArea area = p.getArea();
			
			World playerworld = l.getWorld();
			
			if(!playerworld.equals(loc.getWorld())) continue;
			if(!loc.getBlock().getChunk().isLoaded()) continue;
			
			int tx = loc.getBlockX();
			int ty = loc.getBlockY();
			int tz = loc.getBlockZ();
			
			int x = l.getBlockX();
			int y = l.getBlockY();
			int z = l.getBlockZ();
			
			if(tx != x || ty != y || tz != z) continue;
			
			if(area != null) {
				plugin.man.startGame(e.getPlayer(), area);
			} else {
				plugin.man.startGame(e.getPlayer());
			}
		}
	}
	
	public static String coordsString(Location loc) {
		return coordsString(loc, false);
	}
	public static String coordsString(Location loc, boolean world) {
		if(loc == null) return "null";
		String w = loc.getWorld().getName();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		if(world) {
			return w+": "+x+", "+y+", "+z;
		} else {
			return x+", "+y+", "+z;
		}
	}
	
}
