package us.ajg0702.parkour.game;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

class PkJumpCandidateCharacterizationTest {

    private final World world = mock(World.class);

    @Test
    void distanceFiveOrGreaterIsClampedToFiveAndFlat() {
        PkJump.JumpShape distanceFour = PkJump.shapeForDistance(4);
        PkJump.JumpShape distanceFive = PkJump.shapeForDistance(5);
        PkJump.JumpShape distanceAboveFive = PkJump.shapeForDistance(8);

        assertEquals(4, distanceFour.distance);
        assertEquals(1, distanceFour.maxY);
        assertEquals(5, distanceFive.distance);
        assertEquals(0, distanceFive.maxY);
        assertEquals(5, distanceAboveFive.distance);
        assertEquals(0, distanceAboveFive.maxY);
    }

    @Test
    void distanceGreaterThanFourIsFlat() {
        PkJump.JumpShape shape = PkJump.shapeForDistance(5);

        assertEquals(0, shape.maxY);
    }

    @Test
    void generatedCandidatesAreCardinalOnly() {
        List<Location> candidates = PkJump.candidateLocations(world, 10, 64, 10, 3, 1);

        assertEquals(13, candidates.size());
        for(Location candidate : candidates) {
            boolean changedX = candidate.getBlockX() != 10;
            boolean changedZ = candidate.getBlockZ() != 10;
            assertFalse(changedX && changedZ, "candidate must not be diagonal: " + candidate);
        }
    }

    @Test
    void candidateListHasTwelveUniquePositionsWhenHeightVaries() {
        List<Location> candidates = PkJump.candidateLocations(world, 10, 64, 10, 3, 1);
        Set<Location> unique = new HashSet<>(candidates);

        assertEquals(13, candidates.size());
        assertEquals(12, unique.size());
    }

    @Test
    void flatCandidateListCollapsesToFourUniquePositions() {
        List<Location> candidates = PkJump.candidateLocations(world, 10, 64, 10, 5, 0);
        Set<Location> unique = new HashSet<>(candidates);

        assertEquals(13, candidates.size());
        assertEquals(4, unique.size());
    }
}
