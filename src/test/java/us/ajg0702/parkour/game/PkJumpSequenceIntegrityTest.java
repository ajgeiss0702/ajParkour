package us.ajg0702.parkour.game;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PkJumpSequenceIntegrityTest {

	private final World world = mock(World.class);

	@BeforeEach
	void configureDifficultyEnvelope() {
		Difficulty.EXPERT.setMin(2);
		Difficulty.EXPERT.setMax(5);
	}

	@Test
	void onePlatformSkipIsRejected() {
		Location candidate = loc(5, 64, 0);

		PkJumpSequenceIntegrity.SequenceValidation result = PkJumpSequenceIntegrity.validateAppend(
				Arrays.asList(point(101, "CURRENT", 0, 64, 0), point(102, "PENDING", 3, 64, 0)),
				loc(3, 64, 0),
				candidate,
				Difficulty.EXPERT
		);

		assertFalse(result.accepted);
		assertEquals(PkJumpSequenceIntegrity.NON_ADJACENT_SHORTCUT, result.reason);
		assertEquals(101, result.source.sequenceId);
	}

	@Test
	void fartherNonAdjacentSkipIdentifiesShortcutSource() {
		PkJumpSequenceIntegrity.SequenceValidation result = PkJumpSequenceIntegrity.validateAppend(
				Arrays.asList(
						point(201, "CURRENT", 0, 64, 0),
						point(202, "PENDING", 3, 64, 0),
						point(203, "PENDING", 8, 64, 0)
				),
				loc(8, 64, 0),
				loc(7, 64, 0),
				Difficulty.EXPERT
		);

		assertFalse(result.accepted);
		assertEquals(PkJumpSequenceIntegrity.NON_ADJACENT_SHORTCUT, result.reason);
		assertEquals(202, result.source.sequenceId);
	}

	@Test
	void pendingTrajectoryParticipatesInShortcutDetection() {
		Location candidate = loc(7, 64, 0);

		PkJump.GuardedCandidates result = PkJump.filterGuardedCandidates(
				Collections.singletonList(candidate),
				loc(3, 64, 0),
				loc(8, 64, 0),
				Collections.emptyList(),
				Arrays.asList(
						point(301, "CURRENT", 0, 64, 0),
						point(302, "PENDING", 3, 64, 0),
						point(303, "PENDING", 8, 64, 0)
				),
				Difficulty.EXPERT
		);

		assertTrue(result.candidates.isEmpty());
		assertEquals(PkJumpSequenceIntegrity.NON_ADJACENT_SHORTCUT, result.diagnostics.get(0).reason);
		assertEquals(302, result.diagnostics.get(0).sequenceValidation.source.sequenceId);
	}

	@Test
	void completedHistoryDoesNotDefineImmediatePredecessor() {
		PkJumpSequenceIntegrity.SequenceValidation result = PkJumpSequenceIntegrity.validateAppend(
				Arrays.asList(point(402, "CURRENT", 5, 64, 0), point(403, "PENDING", 10, 64, 0)),
				loc(0, 64, 0),
				loc(15, 64, 0),
				Difficulty.EXPERT
		);

		assertTrue(result.accepted);
		assertEquals(403, result.edges.get(0).from.sequenceId);
	}

	@Test
	void verticalShortcutUsesReachabilityEnvelopeInsteadOfRawHorizontalRadius() {
		PkJumpSequenceIntegrity.SequenceValidation rejected = PkJumpSequenceIntegrity.validateAppend(
				Arrays.asList(point(501, "CURRENT", 0, 64, 0), point(502, "PENDING", 5, 65, 0)),
				loc(5, 65, 0),
				loc(2, 65, 0),
				Difficulty.EXPERT
		);

		PkJumpSequenceIntegrity.SequenceValidation accepted = PkJumpSequenceIntegrity.validateAppend(
				Arrays.asList(point(501, "CURRENT", 0, 64, 0), point(502, "PENDING", 5, 65, 0)),
				loc(5, 65, 0),
				loc(2, 66, 0),
				Difficulty.EXPERT
		);

		assertFalse(rejected.accepted);
		assertEquals(PkJumpSequenceIntegrity.NON_ADJACENT_SHORTCUT, rejected.reason);
		assertTrue(accepted.accepted);
	}

	@Test
	void validLateralSharpTurnTrajectoryPassesFullInvariant() {
		PkJumpSequenceIntegrity.SequenceValidation result = PkJumpSequenceIntegrity.validateFullTrajectory(
				Arrays.asList(
						point(601, "CURRENT", 0, 64, 0),
						point(602, "PENDING", 5, 64, 0),
						point(603, "PENDING", 5, 64, 5),
						point(604, "PENDING", 10, 64, 5)
				),
				Difficulty.EXPERT
		);

		assertTrue(result.accepted);
	}

	@Test
	void validZigZagTrajectoryPassesFullInvariant() {
		PkJumpSequenceIntegrity.SequenceValidation result = PkJumpSequenceIntegrity.validateFullTrajectory(
				Arrays.asList(
						point(701, "CURRENT", 0, 64, 0),
						point(702, "PENDING", 3, 64, 4),
						point(703, "PENDING", 6, 64, 0),
						point(704, "PENDING", 9, 64, 4),
						point(705, "PENDING", 12, 64, 0)
				),
				Difficulty.EXPERT
		);

		assertTrue(result.accepted);
	}

	@Test
	void generatedUniverseSearchesEveryConfiguredDistanceOnce() {
		Difficulty.MEDIUM.setMin(2);
		Difficulty.MEDIUM.setMax(3);

		assertTrue(PkJump.candidateUniverse(world, 0, 64, 0, Difficulty.MEDIUM, 1).contains(loc(2, 64, 0)));
		assertTrue(PkJump.candidateUniverse(world, 0, 64, 0, Difficulty.MEDIUM, 1).contains(loc(3, 64, 0)));
	}

	private PkJumpSequenceIntegrity.TrajectoryPoint point(long sequenceId, String role, int x, int y, int z) {
		return new PkJumpSequenceIntegrity.TrajectoryPoint(sequenceId, role, loc(x, y, z));
	}

	private Location loc(int x, int y, int z) {
		return new Location(world, x, y, z);
	}
}
