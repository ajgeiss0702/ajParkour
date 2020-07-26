package us.ajg0702.parkour;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import us.ajg0702.parkour.game.PkArea;
import us.ajg0702.parkour.game.PkPlayer;

/**
 * This class will be registered through the register-method in the 
 * plugins onEnable-method.
 */
public class Placeholders extends PlaceholderExpansion {

    private Main plugin;

    /**
     * Since we register the expansion inside our own plugin, we
     * can simply use this method here to get an instance of our
     * plugin.
     *
     * @param plugin
     *        The instance of our plugin.
     */
    public Placeholders(Main plugin){
        this.plugin = plugin;
    }

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist(){
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     * 
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest 
     * method to obtain a value if a placeholder starts with our 
     * identifier.
     * <br>This must be unique and can not contain % or _
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier(){
        return "ajpk";
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }
    
    HashMap<Player, HashMap<String, String>> responseCache = new HashMap<>();
    
    public void cleanCache() {
    	Iterator<Player> it = responseCache.keySet().iterator();
    	while(it.hasNext()) {
    		Player p = it.next();
    		if(p == null) {
    			it.remove();
    			continue;
    		}
    		if(!p.isOnline()) {
    			it.remove();
    		}
    	}
    }

    List<String> syncPlaceholders = Arrays.asList("current", "jumping", "jumping_.+");

    /**
     * This is the method called when a placeholder with our identifier 
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  player
     *         A Player.
     * @param  identifier
     *         A String containing the identifier/value.
     *
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player player, final String identifier){
    	//Bukkit.getLogger().info("itentifier: "+identifier);
    	
    	if(regexContains(syncPlaceholders, identifier)) {
    		return this.parsePlaceholder(player, identifier);
    	}
        
        
    	String noc = "_nocache";
    	if(identifier.length() > noc.length()) {
    		int olen = identifier.length()-noc.length();
        	if(identifier.indexOf(noc) == olen) {
        		String idfr = identifier.substring(0, olen);
        		return this.parsePlaceholder(player, idfr);
        	}
    	}
    	
    	Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
    		public void run() {
    			HashMap<String, String> playerCache;
    			if(responseCache.containsKey(player)) {
    				playerCache = responseCache.get(player);
    			} else {
    				playerCache = new HashMap<String, String>();
    			}
    			if(playerCache.size() > 75) {
    				try {
    					playerCache.remove(playerCache.keySet().toArray()[0]);
    				} catch(ConcurrentModificationException e) {
    					Bukkit.getScheduler().runTask(plugin, new Runnable() {
    						public void run() {
    							playerCache.remove(playerCache.keySet().toArray()[0]);
    						}
    					});
    				}
    			}
    			String resp = parsePlaceholder(player, identifier);
    			playerCache.put(identifier, resp);
    			responseCache.put(player, playerCache);
    		}
    	});
    	
    	
    	if(responseCache.containsKey(player)) {
    		HashMap<String, String> playerCache = responseCache.get(player);
    		if(playerCache.containsKey(identifier)) {
    			return playerCache.get(identifier);
    		}
    	}
    	
    	
    	
        
 
        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%) 
        // was provided
        return null;
    }
    
    protected String parsePlaceholder(Player player, String identifier) {
    	if(identifier.matches("stats_top_name_[1-9][0-9]*$")) {
        	int number = Integer.valueOf(identifier.split("stats_top_name_")[1]);
        	Map<String, Double> scores = plugin.scores.getSortedScores(true, null);	
        	Set<String> names = scores.keySet();
        	if(scores.keySet().size() < number || names.toArray()[number-1] == null) {
        		return plugin.msgs.get("placeholders.stats.no-data", player);
        	}

        	String name = names.toArray()[number-1].toString();
            return name;
        }
        if(identifier.matches("stats_top_name_[1-9][0-9]*_.+$")) {
        	int number = Integer.valueOf(identifier.split("_")[3]);
        	String area = identifier.split("_")[4];
        	Map<String, Double> scores = plugin.scores.getSortedScores(true, area);	
        	Set<String> names = scores.keySet();
        	if(scores.keySet().size() < number || names.toArray()[number-1] == null) {
        		return plugin.msgs.get("placeholders.stats.no-data", player);
        	}

        	String name = names.toArray()[number-1].toString();
            return name;
        }
        
        
        

        // %someplugin_placeholder2%
        if(identifier.matches("stats_top_score_[1-9][0-9]*$")) {
        	int number = Integer.valueOf(identifier.split("stats_top_score_")[1]);
        	Map<String, Double> scores = plugin.scores.getSortedScores(true, null);
        	Set<String> plys = scores.keySet();
        	if(scores.keySet().size() < number || plys.toArray()[number-1] == null) {
        		return plugin.msgs.get("placeholders.stats.no-data", player);
        	}
        	String playername = plys.toArray()[number-1].toString();
        	int score = Integer.valueOf((int) Math.round(scores.get(playername)));
        	return score+"";
        }
        if(identifier.matches("stats_top_score_[1-9][0-9]*_.+$")) {
        	int number = Integer.valueOf(identifier.split("_")[3]);
        	String area = identifier.split("_")[4];
        	Map<String, Double> scores = plugin.scores.getSortedScores(true, area);
        	Set<String> plys = scores.keySet();
        	if(scores.keySet().size() < number || plys.toArray()[number-1] == null) {
        		return plugin.msgs.get("placeholders.stats.no-data", player);
        	}
        	String playername = plys.toArray()[number-1].toString();
        	int score = Integer.valueOf((int) Math.round(scores.get(playername)));
        	return score+"";
        }
        
        
        
        if(identifier.matches("stats_gamesplayed")) {
        	return plugin.scores.getGamesPlayed(player.getUniqueId())+"";
        }
        
        
        if(identifier.matches("stats_currentposition")) {
        	Map<String, Double> scores = plugin.scores.getSortedScores(true, null);
        	List<String> plys = new ArrayList<>(scores.keySet());
        	int pos = plys.indexOf(player.getName())+1;
        	if(pos == 0) {
        		return plugin.msgs.get("placeholders.stats.no-data");
        	}
        	return pos+"";
        }
        if(identifier.matches("stats_currentposition_.+$")) {
        	String area = identifier.split("_")[2];
        	Map<String, Double> scores = plugin.scores.getSortedScores(true, area);
        	List<String> plys = new ArrayList<>(scores.keySet());
        	int pos = plys.indexOf(player.getName())+1;
        	if(pos == 0) {
        		return plugin.msgs.get("placeholders.stats.no-data");
        	}
        	return pos+"";
        }
        
        
        
        
        if(identifier.matches("stats_top_time_[1-9][0-9]*$")) {
        	int number = Integer.valueOf(identifier.split("stats_top_time_")[1]);
        	Map<String, Double> scores = plugin.scores.getSortedScores(false, null);
        	Set<String> uuids = scores.keySet();
        	if(scores.keySet().size() < number || uuids.toArray()[number-1] == null) {
        		return plugin.msgs.get("placeholders.stats.no-data", player);
        	}
        	UUID uuid = UUID.fromString(uuids.toArray()[number-1].toString());
        	
        	int time = plugin.scores.getTime(uuid);
        	
        	if(time < 0) {
        		return plugin.msgs.get("placeholders.stats.no-data", player);
        	}
        	
        	int min = (int) Math.floor((time) / (60));
        	int sec = (int) Math.floor((time % (60)));
        	
            return plugin.msgs.get("placeholders.stats.time-format", player)
            		.replaceAll("\\{m\\}", min+"")
            		.replaceAll("\\{s\\}", sec+"");
        }
        /*if(identifier.matches("stats_top_time_[1-9][0-9]*_.+$")) {
        	int number = Integer.valueOf(identifier.split("_")[3]);
        	String area = identifier.split("_")[4];
        	Map<String, Double> scores = plugin.scores.getSortedScores(false, area);
        	Set<String> uuids = scores.keySet();
        	if(scores.keySet().size() < number || uuids.toArray()[number-1] == null) {
        		return plugin.msgs.get("placeholders.stats.no-data", player);
        	}
        	UUID uuid = UUID.fromString(uuids.toArray()[number-1].toString());
        	
        	int time = plugin.scores.getTime(uuid);
        	
        	if(time < 0) {
        		return plugin.msgs.get("placeholders.stats.no-data", player);
        	}
        	
        	int min = (int) Math.floor((time) / (60));
        	int sec = (int) Math.floor((time % (60)));
        	
            return plugin.msgs.get("placeholders.stats.time-format", player)
            		.replaceAll("\\{m\\}", min+"")
            		.replaceAll("\\{s\\}", sec+"");
        }*/
        
        
        
        
        if(identifier.matches("stats_highscore_.+$")) {
        	if(player == null) {
        		return "0";
        	}
        	//Bukkit.getLogger().info("parsing area highscore on "+player.getName());
        	String area = identifier.split("_")[2];
        	int score = plugin.scores.getScore(player.getUniqueId(), area);
        	if(score < 0) {
        		score = 0;
        	}
        	//Bukkit.getLogger().info("parsed: "+score);
        	return score+"";
        }
        
        if(identifier.equals("stats_highscore")) {
        	if(player == null) {
        		return "0";
        	}
        	int score = plugin.scores.getScore(player.getUniqueId(), null);
        	if(score < 0) {
        		score = 0;
        	}
        	return score+"";
        }
        
        
        
        
        if(identifier.equals("current")) {
        	if(player == null) return "0";
        	PkPlayer p = plugin.man.getPlayer(player);
        	if(p != null) {
        		return p.getScore()+"";
        	} else {
        		return plugin.msgs.get("placeholders.current.no-data");
        	}
        	//return "TODO";  //TODO: ajpk_current placeholder
        	/*int p = plugin.plyInParkour(player);
        	if(p == -1) {
        		return plugin.msgs.get("placeholders.current.no-data", player);
        	}
        	return plugin.players.get(p).get("score").toString();*/
        }
        
        if(identifier.equals("jumping")) {
        	return plugin.man.getTotalPlayers()+"";
        }
        if(identifier.equals("jumping_.+")) {
        	String arearaw = identifier.split("_")[1];
        	PkArea area = plugin.man.getArea(arearaw);
        	if(area == null) {
        		return "!";
        	}
        	return plugin.man.getPlayerCounts(area)+"";
        }
        
        
        return null;
    }
    
    public boolean regexContains(List<String> list, String regex) {
    	for(String s : list) {
    		if(s.matches(regex)) return true;
    	}
    	return false;
    }
}
