package us.ajg0702.parkour.top;

import org.bukkit.Bukkit;
import us.ajg0702.parkour.Main;

import java.util.HashMap;

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
    HashMap<String, HashMap<Integer, TopEntry>> cache = new HashMap<>();
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
            if(System.currentTimeMillis() - lastGet.get(area).get(position) > 1000) {
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
}





















