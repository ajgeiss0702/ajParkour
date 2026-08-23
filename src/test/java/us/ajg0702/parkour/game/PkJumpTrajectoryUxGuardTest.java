package us.ajg0702.parkour.game;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PkJumpTrajectoryUxGuardTest {

    private final World world = mock(World.class);

    @Test
    void reverseTurnCandidatesAreFilteredWhenAlternativesRemain() {
        Location previous = new Location(world, 0, 64, 0);
        Location from = new Location(world, 3, 64, 0);
        Location reverse = new Location(world, 0, 64, 0);
        Location straight = new Location(world, 6, 64, 0);
        Location turn = new Location(world, 3, 64, 3);

        PkJump.GuardedCandidates result = PkJump.filterReverseTurnCandidates(
                Arrays.asList(reverse, straight, turn),
                previous,
                from
        );

        List<Location> filtered = result.candidates;
        assertFalse(result.fallbackUsed);
        assertFalse(filtered.contains(reverse));
        assertTrue(filtered.contains(straight));
        assertTrue(filtered.contains(turn));
    }

    @Test
    void guardKeepsOriginalCandidatesWhenAllWouldBeFiltered() {
        Location previous = new Location(world, 0, 64, 0);
        Location from = new Location(world, 3, 64, 0);
        List<Location> candidates = Arrays.asList(
                new Location(world, 0, 64, 0),
                new Location(world, 0, 65, 0)
        );

        PkJump.GuardedCandidates result = PkJump.filterReverseTurnCandidates(candidates, previous, from);

        assertEquals(candidates, result.candidates);
        assertTrue(result.fallbackUsed);
    }

    @Test
    void guardDoesNothingWithoutPreviousHorizontalDirection() {
        Location previous = new Location(world, 3, 64, 0);
        Location from = new Location(world, 3, 65, 0);
        List<Location> candidates = Arrays.asList(new Location(world, 0, 65, 0));

        PkJump.GuardedCandidates result = PkJump.filterReverseTurnCandidates(candidates, previous, from);

        assertSame(candidates, result.candidates);
        assertFalse(result.fallbackUsed);
    }
}
