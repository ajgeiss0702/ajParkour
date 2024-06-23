package us.ajg0702.parkour.game;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.game.difficulties.Difficulty;
import us.ajg0702.parkour.game.difficulties.StaticDifficulty;
import us.ajg0702.parkour.utils.BoxArea;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ParkourArea {

    private final ParkourPlugin plugin;

    private String name;
    @NotNull
    private BoxArea box;
    private Difficulty difficulty;

    private Location fallPosition;

    private final List<ParkourPlayer> players = new CopyOnWriteArrayList<>();

    public ParkourArea(ParkourPlugin plugin, String name, @NotNull BoxArea box, Difficulty difficulty, Location fallPosition) {
        this.plugin = plugin;
        this.name = name;
        this.box = box;
        this.difficulty = difficulty;
        this.fallPosition = fallPosition;
    }

    public @NotNull BoxArea getBox() {
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

    public Location getFallPosition() {
        return fallPosition;
    }

    public void setBox(BoxArea box) {
        this.box = box;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setFallPosition(Location fallPosition) {
        this.fallPosition = fallPosition;
    }

    public void startPlayer(Player player) {
        ParkourPlayer parkourPlayer = new ParkourPlayer(plugin, this, player);
        players.add(parkourPlayer);
    }
}
