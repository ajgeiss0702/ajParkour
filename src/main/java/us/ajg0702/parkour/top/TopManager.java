package us.ajg0702.parkour.top;

import org.bukkit.Bukkit;
import us.ajg0702.parkour.Main;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
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



    private ConcurrentHashMap<UUID, HashMap<String, Integer>> highScores = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, HashMap<String, Long>> lastGetHS = new ConcurrentHashMap<>();
    public int getHighScore(UUID playerId, String area) {
        if(area == null) area = "overall";

        if(!highScores.containsKey(playerId)) {
            highScores.put(playerId, new HashMap<>());
        }
        if(!lastGetHS.containsKey(playerId) || lastGetHS.get(playerId) == null) {
            lastGetHS.put(playerId, new HashMap<>());
        }

        if(highScores.get(playerId).containsKey(area) && lastGetHS.get(playerId).containsKey(area)) {
            if(Calendar.getInstance().getTimeInMillis() -
                    lastGetHS.get(playerId).get(area)
                    > 1000) {
                lastGetHS.get(playerId).put(area, System.currentTimeMillis());
                fetchHighScoreAsync(playerId, area);
            }
            return highScores.get(playerId).get(area);
        }

        lastGetHS.get(playerId).put(area, System.currentTimeMillis());
        return fetchHighScore(playerId, area);
    }

    long lastClean = 0;

    private void fetchHighScoreAsync(UUID playerId, String area) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> fetchHighScore(playerId, area));
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if(System.currentTimeMillis() - lastClean > 300e3) {
                lastClean = System.currentTimeMillis();

                for(UUID key : highScores.keySet()) {
                    if(Bukkit.getPlayer(key) == null) {
                        highScores.remove(key);
                    }
                }
                for(UUID key : lastGetHS.keySet()) {
                    if(Bukkit.getPlayer(key) == null) {
                        lastGetHS.remove(key);
                    }
                }
            }
        });
    }
    private int fetchHighScore(UUID playerId, String area) {
        int hs = plugin.scores.getHighScore(playerId, area);
        if(!highScores.containsKey(playerId)) {
            highScores.put(playerId, new HashMap<>());
        }
        highScores.get(playerId).put(area, hs);
        return hs;
    }

    public void clearPlayerCache(UUID playerId) {
        highScores.remove(playerId);
        lastGetHS.remove(playerId);
    }



    public ConcurrentHashMap<UUID, HashMap<String, Integer>> getHighScores() {
        return highScores;
    }
    public ConcurrentHashMap<UUID, HashMap<String, Long>> getLastGetHS() {
        return lastGetHS;
    }

    public HashMap<String, HashMap<Integer, Long>> getLastGet() {
        return lastGet;
    }
    public HashMap<String, HashMap<Integer, TopEntry>> getCache() {
        return cache;
    }
}





















