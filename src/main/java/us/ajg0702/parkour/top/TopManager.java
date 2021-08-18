package us.ajg0702.parkour.top;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.ajg0702.parkour.Main;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TopManager {
    private static TopManager instance;
    public static TopManager getInstance() {
        return instance;
    }
    public static TopManager getInstance(Main pl) {
        if(instance == null) {
            instance = new TopManager(pl);
        }
        return instance;
    }

    private final Main plugin;
    private TopManager(Main pl) {
        plugin = pl;
    }

    private HashMap<String, HashMap<Integer, Long>> lastGet = new HashMap<>();
    private HashMap<String, HashMap<Integer, TopEntry>> cache = new HashMap<>();
    public TopEntry getTop(int position, String area) {
        if(area == null) {
            area = "overall";
        }

        if(!cache.containsKey(area)) {
            cache.put(area, new HashMap<>());
        }
        if(!lastGet.containsKey(area)) {
            lastGet.put(area, new HashMap<>());
        }

        if(cache.get(area).containsKey(position)) {
            if(System.currentTimeMillis() - lastGet.get(area).get(position) > 5000) {
                lastGet.get(area).put(position, System.currentTimeMillis());
                fetchPositionAsync(position, area);
            }
            return cache.get(area).get(position);
        }

        lastGet.get(area).put(position, System.currentTimeMillis());
        return fetchPosition(position, area);
    }

    private void fetchPositionAsync(int position, String area) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> fetchPosition(position, area));
    }
    private TopEntry fetchPosition(int position, String area) {
        TopEntry te = plugin.scores.getTopPosition(position, area);
        cache.get(area).put(position, te);
        return te;
    }



    private ConcurrentHashMap<Player, HashMap<String, Integer>> highScores = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Player, HashMap<String, Long>> lastGetHS = new ConcurrentHashMap<>();
    public int getHighScore(Player player, String area) {
        if(area == null) area = "overall";

        if(!highScores.containsKey(player)) {
            highScores.put(player, new HashMap<>());
        }
        if(!lastGetHS.containsKey(player) || lastGetHS.get(player) == null) {
            lastGetHS.put(player, new HashMap<>());
        }

        if(highScores.get(player).containsKey(area) && lastGetHS.get(player).containsKey(area)) {
            if(Calendar.getInstance().getTimeInMillis() -
                    lastGetHS.get(player).get(area)
                    > 1000) {
                lastGetHS.get(player).put(area, System.currentTimeMillis());
                fetchHighScoreAsync(player, area);
            }
            return highScores.get(player).get(area);
        }

        lastGetHS.get(player).put(area, System.currentTimeMillis());
        return fetchHighScore(player, area);
    }

    long lastClean = 0;

    private void fetchHighScoreAsync(Player player, String area) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> fetchHighScore(player, area));
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if(System.currentTimeMillis() - lastClean > 300e3) {
                lastClean = System.currentTimeMillis();

                for(Player key : highScores.keySet()) {
                    if(!key.isOnline()) {
                        highScores.remove(player);
                    }
                }
                for(Player key : lastGetHS.keySet()) {
                    if(!key.isOnline()) {
                        lastGetHS.remove(player);
                    }
                }
            }
        });
    }
    private int fetchHighScore(Player player, String area) {
        int hs = plugin.scores.getHighScore(player.getUniqueId(), area);
        if(!highScores.containsKey(player)) {
            highScores.put(player, new HashMap<>());
        }
        highScores.get(player).put(area, hs);
        return hs;
    }

    public void clearPlayerCache(Player ply) {
        highScores.remove(ply);
    }



    public HashMap<Player, HashMap<String, Integer>> getHighScores() {
        return new HashMap<>(highScores);
    }
    public HashMap<Player, HashMap<String, Long>> getLastGetHS() {
        return new HashMap<>(lastGetHS);
    }

    public HashMap<String, HashMap<Integer, Long>> getLastGet() {
        return lastGet;
    }
    public HashMap<String, HashMap<Integer, TopEntry>> getCache() {
        return cache;
    }
}





















