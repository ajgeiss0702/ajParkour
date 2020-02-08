package us.ajg0702.parkour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import us.ajg0702.parkour.game.Manager;
import us.ajg0702.parkour.utils.Config;
import us.ajg0702.parkour.utils.Updater;
import us.ajg0702.parkour.utils.VersionSupport;

public class Main extends JavaPlugin {
	
	public boolean papi = false;
	public Messages msgs;
	public Scores scores;
	public Manager man;
	
	public AreaStorage areaStorage;

	public Config config = null;
	
	public Commands cmds;
	
	public BlockSelector selector;
	
	public Rewards rewards;
	
	Updater updater;
	
	@Override
	public void onEnable() {
		
		String popSound = "ENTITY_CHICKEN_EGG";
		//System.out.println("Minor Version: 1."+VersionSupport.getMinorVersion());
		
		if(VersionSupport.getMinorVersion() <= 8) {
			popSound = "CHICKEN_EGG_POP";
		}
		
		config = new Config(this, "config.yml");
		config.addEntry("area-selection", "lowest", "The method to fill multiple multiple parkour areas.\nIf you only have one, this option is ignored.\n Default: lowest");
		config.addEntry("random-block-selection", "each", "Whether to pick a random block each jump, or a random block at the start.\n Options: 'each' or 'start'.\n Default: each");
		config.addEntry("random-item", "VINE", "This is the item to show in the selector GUI to represent the random block mode.\n Default: VINE");
		config.addEntry("jump-sound", popSound, "This is the sound to play when a player makes a jump.\n"
				+ "Here is a list for the latest spigot version: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html "
				+ "(the list starts below the orange box that says Enum Constants)\n"
				+ " Default: "+popSound);
		config.addEntry("top-shown", 10, "The amount of players to show in /ajParkour top\n Default: 10");
		config.addEntry("jumps-ahead", 1, "The number of extra blocks to place ahead of the next jump.\n Default: 1");
		config.addEntry("start-sound", "NONE", "The sound to play when a player starts parkour. See jump-sound for more info.\n Default: NONE");
		config.addEntry("end-sound", "NONE", "The sound to play when a player falls. See jump-sound for more info.\n Default: NONE");
		config.addEntry("particle-count", 25, "The number of particles to spawn when a new block is placed.\n Default: 25");
		config.addEntry("execute-reward-commands", "earned", "When to execute the reward commands.\n Options: 'earned', 'after'\n Default: 'earned'");
		config.addEntry("parkour-inventory", false, "If this is true, the player's inventory will be cleared while on the parkour, and reset after.\nNOTICE: If one of your reward commands gives items, you need to set execute-reward-commands to 'after' or else they will lose the items.\n Default: false");
		config.addEntry("start-disabled-worlds", "disabledworld1,disabledworld2", "If a world is listed here, the /ajParkour start command will not be usable from that world.\nWorld names are seperated by commas (without spaces) and are case-sensitive!\n Example: 'disabledworld1,disabledworld2'");
		config.addEntry("kick-time", 60, "How long, in seconds, after a player doesnt move should we kick them from the parkour?\nSet to -1 to disable\n Default: 60");
		config.addEntry("notify-update", true, "Should we notify people with the permission ajparkour.update that an update is available?\nThey will then be able to download it using /ajParkour update\n Default: true");
		config.addEntry("begin-score-per-area", false, "Should the score we tell the player to beat be per-area or global?\nFor example, if this is true and the player got 30 on another area but only 10 on this one, they will be told to beat their record of 10.\n Default: false");
		config.addEntry("enable-portals", true, "Should the portals be disabled?\nIf your server is lagging from this plugin without many people on parkour, try disabling this.\nREQUIRES SERVER RESTART (not just config reload)\n Default: true");
		config.addEntry("faster-portals", false, "Shoud we use a more optimized method to look if players are at a portal?\nIt may require the player to be in the block for a little longer\nEnable this if you have a lot of people on your server and are experiencing lag.\n Default: false");
		config.addEntry("enable-updater", true, "Should the updater be enabled?\nIf this is disabled, the plugin will not attempt to check for updates, and you will have to download new updates manually\nRequires a restart\n Default: true");
		config.setEntries();
		
		msgs = new Messages(this);
		scores = new Scores(this);
		
		man = new Manager(this);
		
		selector = new BlockSelector(this);
		
		areaStorage = new AreaStorage(this);
		
		rewards = new Rewards(this);
		
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
			public void run() {
				areaStorage.getAreas();
				areaStorage.getPortals();
			}
		}, 10);
		
		getCommand("ajParkour").setTabCompleter(new CommandComplete(this));
		
		if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            new Placeholders(this).register(); //TODO: finish papi placeholders
            this.papi = true;
		}
		
		getServer().getPluginManager().registerEvents(man, this);
		getServer().getPluginManager().registerEvents(selector, this);
		
		cmds = new Commands(this);
		
		
		new Metrics(this);
		
		updater = new Updater(this);
		
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', 
				"&aajParkour &2v"+this.getDescription().getVersion()+" by ajgeiss0702 has been &aenabled!"));
	}

	public Config getAConfig() {
		return this.config;
	}
	
	
	
	public LinkedHashMap<String, Double> sortByValue(HashMap<String, Double> passedMap) {
	    List<String> mapKeys = new ArrayList<>(passedMap.keySet());
	    List<Double> mapValues = new ArrayList<>(passedMap.values());
	    Collections.sort(mapValues);
	    //Collections.sort(mapKeys);

	    LinkedHashMap<String, Double> sortedMap =
	        new LinkedHashMap<>();

	    Iterator<Double> valueIt = mapValues.iterator();
	    while (valueIt.hasNext()) {
	        Double val = valueIt.next();
	        Iterator<String> keyIt = mapKeys.iterator();

	        while (keyIt.hasNext()) {
	            String key = keyIt.next();
	            Double comp1 = passedMap.get(key);
	            Double comp2 = val;

	            if (comp1.equals(comp2)) {
	                keyIt.remove();
	                sortedMap.put(key, val);
	                break;
	            }
	        }
	    }
	    LinkedHashMap<String, Double> reverseMap = new LinkedHashMap<>();
	    List<Entry<String,Double>> list = new ArrayList<>(sortedMap.entrySet());

	    for( int i = list.size() -1; i >= 0 ; i --){
	        Entry<String,Double> e = list.get(i);
	        reverseMap.put(e.getKey(), e.getValue());
	    }
	    return reverseMap;
	}
	public LinkedHashMap<Object, Double> sortByValueWithObjectKey(HashMap<Object, Double> passedMap) {
		return sortByValueWithObjectKey(passedMap, true);
	}
	public LinkedHashMap<Object, Double> sortByValueWithObjectKey(HashMap<Object, Double> passedMap, boolean reverse) {
	    List<Object> mapKeys = new ArrayList<>(passedMap.keySet());
	    List<Double> mapValues = new ArrayList<>(passedMap.values());
	    Collections.sort(mapValues);

	    LinkedHashMap<Object, Double> sortedMap =
	        new LinkedHashMap<>();

	    Iterator<Double> valueIt = mapValues.iterator();
	    while (valueIt.hasNext()) {
	        Double val = valueIt.next();
	        Iterator<Object> keyIt = mapKeys.iterator();

	        while (keyIt.hasNext()) {
	            Object key = keyIt.next();
	            Double comp1 = passedMap.get(key);
	            Double comp2 = val;

	            if (comp1.equals(comp2)) {
	                keyIt.remove();
	                sortedMap.put(key, val);
	                break;
	            }
	        }
	    }
	    if(reverse) {
	    	LinkedHashMap<Object, Double> reverseMap = new LinkedHashMap<>();
		    List<Entry<Object,Double>> list = new ArrayList<>(sortedMap.entrySet());

		    for( int i = list.size() -1; i >= 0 ; i --){
		        Entry<Object,Double> e = list.get(i);
		        reverseMap.put(e.getKey(), e.getValue());
		    }
		    return reverseMap;
	    } else {
	    	return sortedMap;
	    }
	}
	
	@Override
	public void onDisable() {
		man.disable();
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', 
				"&cajParkour &4v"+this.getDescription().getVersion()+" by ajgeiss0702 has been &cdisabled!"));
	}
	
	
	public static String nullstring(Object n) {
		if(n == null) {
			return "null";
		} else {
			return n.toString();
		}
	}
	
	
	
	public static int random(int min, int max) {


		if (min > max) {
			throw new IllegalArgumentException("max must be greater than min: "+min+"-"+max);
		} else if(min == max) {
			return min;
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

}
