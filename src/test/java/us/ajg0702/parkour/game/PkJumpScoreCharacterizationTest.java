package us.ajg0702.parkour.game;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PkJumpScoreCharacterizationTest {

    private World world;
    private PkArea area;

    @BeforeEach
    void setUp() {
        world = mock(World.class);
        Block air = block(Material.AIR);
        when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenReturn(air);
        when(world.getBlockAt(any(Location.class))).thenReturn(air);

        area = new PkArea(
                "test",
                new Location(world, 0, 0, 0),
                new Location(world, 10, 10, 10),
                null,
                Difficulty.EASY,
                -1
        );

        Manager manager = mock(Manager.class);
        when(manager.getPlayersInArea(any(PkArea.class))).thenReturn(Collections.emptyList());
        Manager.instance = manager;
    }

    @AfterEach
    void tearDown() {
        Manager.instance = null;
    }

    @Test
    void outOfAreaCandidateSubtractsTenWithoutDiscardingPriorScore() {
        Location from = new Location(world, 10, 5, 5);
        Location outside = new Location(world, 20, 5, 5);

        assertEquals(-18, PkJump.getBlockScore(outside, from, area, null, 0f));
    }

    @Test
    void blockedAirColumnReturnsImmediatelyWithObstaclePenalty() {
        Block stone = block(Material.STONE);
        when(world.getBlockAt(10, 5, 5)).thenReturn(stone);
        when(world.getBlockAt(new Location(world, 10, 5, 5))).thenReturn(stone);

        Location from = new Location(world, 7, 5, 5);
        Location blocked = new Location(world, 10, 5, 5);

        assertEquals(-40, PkJump.getBlockScore(blocked, from, area, null, 90f));
    }

    private static Block block(Material material) {
        Block block = mock(Block.class);
        when(block.getType()).thenReturn(material);
        return block;
    }
}
