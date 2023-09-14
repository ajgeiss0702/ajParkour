package us.ajg0702.parkour.game;

import us.ajg0702.parkour.utils.BlockDirection;
import us.ajg0702.parkour.utils.BoxArea;
import us.ajg0702.parkour.utils.WorldPosition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static us.ajg0702.parkour.utils.Utils.getBlocksInLine;

public class BlockPossibility {

    private final WorldPosition position;

    private final ParkourPlayer player;

    private final BlockDirection direction;

    private int score = 30;

    public BlockPossibility(
            ParkourPlayer player,
            ParkourBlock previous,
            WorldPosition start,
            BlockDirection direction,
            int xChange,
            int yChange,
            int zChange
    ) {
        this.player = player;
        this.direction = direction;
        position = new WorldPosition(
                start.getWorldName(),
                start.getX() + xChange,
                start.getY() + yChange,
                start.getZ() + zChange
        );

        BoxArea area = player.getArea().getBox();

        int closestBorder = area.getClosestWallDistance(position);
        if(closestBorder <= 5) {
            int scorePenalty = (int) Math.round(-2 * Math.pow(closestBorder - 5, 2)); // -2(x-5)^{2}
            score -= Math.abs(scorePenalty);
        }

        if(previous != null) {
            BlockDirection previousDirection = previous.getDirection();
            List<BlockDirection> closeDirections = new ArrayList<>(Arrays.asList(
                    previousDirection,
                    previousDirection.getRight90(),
                    previousDirection.getLeft90()
            ));
            if(player.plugin.getAConfig().getBoolean("allow-diagonal-jumps")) {
                closeDirections.add(previousDirection.getRight());
                closeDirections.add(previousDirection.getLeft());
            }

            BlockDirection opposite = previousDirection.get180();
            List<BlockDirection> badDirections = Arrays.asList(
                    opposite,
                    opposite.getRight(),
                    opposite.getLeft()
            );

            if(!closeDirections.contains(direction)) {
                score -= 5;
            }
            if(badDirections.contains(direction)) {
                score -= 10;
            }
        }

        List<WorldPosition> shouldBeAir = new ArrayList<>();
        if(previous != null) {
            List<WorldPosition> previousJumpables = getNearbyJumpables(previous.getPosition());
            List<WorldPosition> jumpables = getNearbyJumpables(position);
            for (int i = 0; i < jumpables.size(); i++) {
                WorldPosition previousPosition = previousJumpables.get(i);
                WorldPosition currentPosition = jumpables.get(i);
                shouldBeAir.addAll(getBlocksInLine(previousPosition, currentPosition));
            }
        } else {
            shouldBeAir = getNearbyJumpables(position);
        }
        if(position.getLocation().getBlock().getType().isSolid()) {
            score -= 50;
        }
        for (WorldPosition jumpable : shouldBeAir) {
            if(jumpable.getLocation().getBlock().getType().isSolid()) {
                score -= 10;
            }
        }

        // TODO: avoid other players in the same area

    }

    public int getScore() {
        return score;
    }

    public BlockDirection getDirection() {
        return direction;
    }

    public WorldPosition getPosition() {
        return position;
    }

    private static List<WorldPosition> getNearbyJumpables(WorldPosition position) {
        List<WorldPosition> positions = new ArrayList<>();
        for (int y = 0; y < 4; y++) {
            positions.addAll(Arrays.asList(
                    position.getRelative(0, y, 0),
                    position.getRelative(0, y, 1),
                    position.getRelative(1, y, 1),
                    position.getRelative(1, y, 0),
                    position.getRelative(0, y, -1),
                    position.getRelative(-1, y, -1),
                    position.getRelative(-1, y, 0)
            ));
        }
        return positions;
    }
}
