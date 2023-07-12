package us.ajg0702.parkour.game;

import org.bukkit.entity.Player;
import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.game.difficulties.Difficulty;
import us.ajg0702.parkour.utils.BoxArea;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ParkourArea {

    private final ParkourPlugin plugin;

    private String name;
    private BoxArea box;
    private Difficulty difficulty;

    private final List<ParkourPlayer> players = new CopyOnWriteArrayList<>();

    public ParkourArea(ParkourPlugin plugin, String name, BoxArea box, Difficulty difficulty) {
        this.plugin = plugin;
        this.name = name;
        this.box = box;
        this.difficulty = difficulty;
    }

    public BoxArea getBox() {
        return box;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getName() {
        return name;
    }

    public List<ParkourPlayer> getPlayers() {
        return players;
    }

    public void startPlayer(Player player) {
        ParkourPlayer parkourPlayer = new ParkourPlayer(plugin, this, player);
        players.add(parkourPlayer);
    }
}
