package us.ajg0702.parkour.game;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PkPlayerProgressionCharacterizationTest {

    private final World world = mock(World.class);

    @Test
    void normalJumpAdvancesInsideHorizontalTargetZone() {
        Location goal = new Location(world, 10, 64, 10);

        assertTrue(PkPlayer.isInsideMadeItZone(new Location(world, 11.29, 80, 11.29), goal));
    }

    @Test
    void targetZoneBoundaryIsStrictlyLessThanPointEight() {
        Location goal = new Location(world, 10, 64, 10);

        assertFalse(PkPlayer.isInsideMadeItZone(new Location(world, 11.30, 64, 10.50), goal));
        assertFalse(PkPlayer.isInsideMadeItZone(new Location(world, 10.50, 64, 11.30), goal));
    }

    @Test
    void landingOnFutureJumpDoesNotAdvanceAgainstCurrentTarget() {
        Location currentTarget = new Location(world, 10, 64, 10);
        Location futureJump = new Location(world, 13, 64, 10);

        assertFalse(PkPlayer.isInsideMadeItZone(
                new Location(world, futureJump.getX() + 0.5, futureJump.getY(), futureJump.getZ() + 0.5),
                currentTarget
        ));
    }

    @Test
    void backtrackingFromFutureJumpIntoTargetZoneAdvances() {
        Location currentTarget = new Location(world, 10, 64, 10);

        assertTrue(PkPlayer.isInsideMadeItZone(new Location(world, 10.5, 64, 10.5), currentTarget));
    }

    @Test
    void verticalPositionDoesNotAffectProgression() {
        Location goal = new Location(world, 10, 64, 10);

        assertTrue(PkPlayer.isInsideMadeItZone(new Location(world, 10.5, -20, 10.5), goal));
        assertTrue(PkPlayer.isInsideMadeItZone(new Location(world, 10.5, 200, 10.5), goal));
    }

    @Test
    void fallEndsBelowCurrentBlockWhenFlyingOrAboveHighestBlockPlusThree() {
        assertTrue(PkPlayer.shouldEndForFall(new Location(world, 0, 62, 0), false, 64, 67, 1));
        assertTrue(PkPlayer.shouldEndForFall(new Location(world, 0, 65, 0), true, 64, 67, 1));
        assertTrue(PkPlayer.shouldEndForFall(new Location(world, 0, 71, 0), false, 64, 67, 1));

        assertFalse(PkPlayer.shouldEndForFall(new Location(world, 0, 63, 0), false, 64, 67, 1));
        assertFalse(PkPlayer.shouldEndForFall(new Location(world, 0, 70, 0), false, 64, 67, 1));
    }
}
