package us.ajg0702.parkour.game;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void guardDoesNotFallbackToRejectedCandidatesWhenAllWouldBeFiltered() {
        Location previous = new Location(world, 0, 64, 0);
        Location from = new Location(world, 3, 64, 0);
        List<Location> candidates = Arrays.asList(
                new Location(world, 0, 64, 0),
                new Location(world, 0, 65, 0)
        );

        PkJump.GuardedCandidates result = PkJump.filterReverseTurnCandidates(candidates, previous, from);

        assertTrue(result.candidates.isEmpty());
        assertFalse(result.fallbackUsed);
        assertEquals(2, result.rejectedCount);
    }

    @Test
    void guardDoesNothingWithoutPreviousHorizontalDirection() {
        Location previous = new Location(world, 3, 64, 0);
        Location from = new Location(world, 3, 65, 0);
        List<Location> candidates = Arrays.asList(new Location(world, 0, 65, 0));

        PkJump.GuardedCandidates result = PkJump.filterReverseTurnCandidates(candidates, previous, from);

        assertEquals(candidates, result.candidates);
        assertFalse(result.fallbackUsed);
    }

    @Test
    void recentRegionCandidatesAreFilteredEvenWithoutExactUTurn() {
        Location previous = new Location(world, 9, 64, 3);
        Location from = new Location(world, 9, 64, 0);
        Location nearRecent = new Location(world, 3, 64, 0);
        Location away = new Location(world, 15, 64, 0);

        PkJump.GuardedCandidates result = PkJump.filterNayatsuUxGuardCandidates(
                Arrays.asList(nearRecent, away),
                previous,
                from,
                Arrays.asList(new Location(world, 3, 64, 0), new Location(world, 6, 64, 0))
        );

        assertFalse(result.candidates.contains(nearRecent));
        assertTrue(result.candidates.contains(away));
        assertEquals("REJECT_RECENT_REGION", result.diagnostics.get(0).reason);
    }

    @Test
    void recentHistoryCarriesAcrossConsecutiveGenerations() {
        ArrayDeque<Location> history = new ArrayDeque<>();
        PkPlayer.recordRecentJump(history, new Location(world, 0, 64, 0), 5);
        PkPlayer.recordRecentJump(history, new Location(world, 3, 64, 0), 5);
        PkPlayer.recordRecentJump(history, new Location(world, 6, 64, 0), 5);

        Location previous = new Location(world, 9, 64, 3);
        Location from = new Location(world, 9, 64, 0);
        Location ambiguousReturn = new Location(world, 3, 64, 0);
        Location forward = new Location(world, 15, 64, 0);

        PkJump.GuardedCandidates result = PkJump.filterNayatsuUxGuardCandidates(
                Arrays.asList(ambiguousReturn, forward),
                previous,
                from,
                Arrays.asList(history.toArray(new Location[0]))
        );

        assertFalse(result.candidates.contains(ambiguousReturn));
        assertTrue(result.candidates.contains(forward));
    }

    @Test
    void rejectedCandidateCannotWinBecauseItHasHighestLegacyScore() {
        Location rejected = new Location(world, 0, 64, 0);
        Location eligible = new Location(world, 6, 64, 0);
        LinkedHashMap<Object, Double> sortedScores = new LinkedHashMap<>();
        sortedScores.put(eligible, 10.0);
        sortedScores.put(rejected, 100.0);

        Map<Object, Double> selectedPool = PkJump.selectHighestEligibleScores(
                sortedScores,
                Arrays.asList(eligible),
                100.0
        );

        assertFalse(selectedPool.containsKey(rejected));
        assertTrue(selectedPool.containsKey(eligible));
        assertEquals(1, selectedPool.size());
    }
}
