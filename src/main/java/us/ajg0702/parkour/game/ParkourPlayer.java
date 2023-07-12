package us.ajg0702.parkour.game;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.utils.WorldPosition;
import us.ajg0702.utils.spigot.LocUtils;

import java.util.ArrayList;
import java.util.List;

import static us.ajg0702.parkour.ParkourPlugin.message;

public class ParkourPlayer {
    protected final ParkourPlugin plugin;
    private final ParkourArea area;
    private final Player player;

    private int score = 0;

    private final List<ParkourBlock> blocks = new ArrayList<>();

    public ParkourPlayer(ParkourPlugin plugin, ParkourArea area, Player player) {
        this.plugin = plugin;
        this.area = area;
        this.player = player;

        int blockCount = plugin.getAConfig().getInt("jumps-ahead") + 3;
        for (int i = 0; i < blockCount; i++) {
            addBlock();
        }

        Location teleportLocation = blocks.get(0).getPosition()
                .getRelative(0, 1, 0).getLocation();
        teleportLocation.setYaw(blocks.get(1).getDirection().getYaw());
        teleportLocation.setPitch(20f);

        player.teleport(LocUtils.center(teleportLocation));

    }

    public synchronized void addBlock() {
        if(blocks.size() > 0) {
            blocks.get(blocks.size() - 1).place();
        }
        ParkourBlock previousBlock = blocks.size() > 0 ? blocks.get(blocks.size() - 1) : null;
        ParkourBlock block = new ParkourBlock(previousBlock, plugin, area, this);
        blocks.add(block);
    }

    public ParkourArea getArea() {
        return area;
    }

    public Player getPlayer() {
        return player;
    }

    public void end() {
        for (ParkourBlock block : blocks) {
            block.remove();
        }
        area.getPlayers().remove(this);
    }

    public synchronized void checkMadeIt() {
        Location loc = player.getLocation();
        int x = loc.getBlockX();
        double y = loc.getY();
        int z =  loc.getBlockZ();

        while(blocks.size() < 2) {
            addBlock();
        }

        WorldPosition target = blocks.get(1).getPosition();

        if(
                (x != target.getX()) ||
                        (z != target.getZ()) ||
                        !(y > target.getY() && y < (target.getY() + 2d))
        ) {
            // Player hasn't made it yet

            int lowest = Integer.MAX_VALUE;
            for (ParkourBlock block : blocks) {
                int blockY = block.getPosition().getY();
                if(blockY < lowest) {
                    lowest = blockY;
                }
            }

            if(y < lowest) {
                player.sendMessage("you fell :(");
                end();
            }

            return;
        }

        // Player made the jump!

        score++;

        sendActionbar();

        int correctSize = plugin.getAConfig().getInt("jumps-ahead") + 3;

        blocks.get(0).remove();
        blocks.remove(0);

        while(blocks.size() < correctSize) {
            addBlock();
        }
    }

    public void sendActionbar() {
        plugin.getAdventure().player(player)
                .sendActionBar(
                        message("&7Score &f " + score)
                );
    }

    public int getScore() {
        return score;
    }
}
