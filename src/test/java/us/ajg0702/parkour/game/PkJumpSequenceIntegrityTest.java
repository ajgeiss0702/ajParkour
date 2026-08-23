package us.ajg0702.parkour.game;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	@Test
	void reachabilityBoundaryMatrixCoversHorizontalAndVerticalEdges() {
		assertReachable(2, 0);
		assertReachable(3, 0);
		assertReachable(4, 0);
		assertReachable(5, 0);
		assertUnreachable(6, 0);

		assertReachable(3, 1);
		assertReachable(4, 1);
		assertUnreachable(5, 1);

		assertReachable(3, -1);
		assertReachable(4, -1);
		assertReachable(5, -1);
		assertReachable(6, -1);
		assertUnreachable(7, -1);

		assertReachable(3, -2);
		assertReachable(4, -2);
		assertReachable(5, -2);
		assertReachable(7, -2);
		assertUnreachable(8, -2);
	}

	@Test
	void diagonalReachabilityUsesPhysicalDistanceNotTemplateIdentity() {
		assertTrue(PkJumpSequenceIntegrity.canReach(loc(0, 64, 0), loc(3, 64, 4), Difficulty.EXPERT));
		assertFalse(PkJump.candidateUniverse(world, 0, 64, 0, Difficulty.EXPERT, 1).contains(loc(3, 64, 4)));
	}

	@Test
	void generatedUniverseIsCompleteForCurrentCardinalGenerationContract() {
		Difficulty.MEDIUM.setMin(2);
		Difficulty.MEDIUM.setMax(3);

		List<Location> universe = PkJump.candidateUniverse(world, 0, 64, 0, Difficulty.MEDIUM, 1);
		Set<Location> unique = new HashSet<>(universe);

		assertEquals(unique.size(), universe.size());
		assertEquals(24, universe.size());
		for(int distance : Arrays.asList(2, 3)) {
			for(int y : Arrays.asList(63, 64, 65)) {
				assertTrue(universe.contains(loc(distance, y, 0)));
				assertTrue(universe.contains(loc(-distance, y, 0)));
				assertTrue(universe.contains(loc(0, y, distance)));
				assertTrue(universe.contains(loc(0, y, -distance)));
			}
		}
		assertFalse(universe.contains(loc(2, 64, 2)));
	}

	@Test
	void generatedUniverseRespectsFlatLongJumpContract() {
		Difficulty.EXPERT.setMin(5);
		Difficulty.EXPERT.setMax(5);

		List<Location> universe = PkJump.candidateUniverse(world, 0, 64, 0, Difficulty.EXPERT, 1);

		assertEquals(4, universe.size());
		assertTrue(universe.contains(loc(5, 64, 0)));
		assertTrue(universe.contains(loc(-5, 64, 0)));
		assertTrue(universe.contains(loc(0, 64, 5)));
		assertTrue(universe.contains(loc(0, 64, -5)));
		assertFalse(universe.contains(loc(5, 65, 0)));
	}

	@Test
	void avoidableDeadEndIsRejectedBeforeLegacySelection() {
		Location deadEnd = loc(8, 64, 0);
		Location viable = loc(3, 64, 5);
		List<PkJumpSequenceIntegrity.TrajectoryPoint> active = Arrays.asList(
				point(801, "CURRENT", 0, 64, 0),
				point(802, "PENDING", 3, 64, 0)
		);
		List<Location> spatialHistory = PkJump.candidateUniverse(world, deadEnd.getBlockX(), deadEnd.getBlockY(), deadEnd.getBlockZ(), Difficulty.EXPERT, 1);
		PkJump.GuardedCandidates guarded = new PkJump.GuardedCandidates(
				Arrays.asList(deadEnd, viable),
				Arrays.asList(deadEnd, viable),
				0,
				false,
				Arrays.asList(acceptedDiagnostic(deadEnd), acceptedDiagnostic(viable))
		);

		PkJump.GuardedCandidates result = PkJump.filterOneStepViableCandidates(
				guarded,
				spatialHistory,
				active,
				Difficulty.EXPERT
		);

		assertFalse(result.candidates.contains(deadEnd));
		assertTrue(result.candidates.contains(viable));
		assertEquals("REJECT_DEAD_END_CANDIDATE", result.diagnostics.get(0).reason);
	}

	@Test
	void allDeadEndCandidatesDoNotFallbackToUnsafeCandidate() {
		Location deadEnd = loc(8, 64, 0);
		List<PkJumpSequenceIntegrity.TrajectoryPoint> active = Arrays.asList(
				point(811, "CURRENT", 0, 64, 0),
				point(812, "PENDING", 3, 64, 0)
		);
		List<Location> spatialHistory = PkJump.candidateUniverse(world, deadEnd.getBlockX(), deadEnd.getBlockY(), deadEnd.getBlockZ(), Difficulty.EXPERT, 1);
		PkJump.GuardedCandidates guarded = new PkJump.GuardedCandidates(
				Collections.singletonList(deadEnd),
				Collections.singletonList(deadEnd),
				0,
				false,
				Collections.singletonList(acceptedDiagnostic(deadEnd))
		);

		PkJump.GuardedCandidates result = PkJump.filterOneStepViableCandidates(
				guarded,
				spatialHistory,
				active,
				Difficulty.EXPERT
		);

		assertTrue(result.candidates.isEmpty());
		assertFalse(result.fallbackUsed);
	}

	@Test
	void viableLateralContinuationSurvivesLookahead() {
		Location lateral = loc(5, 64, 5);
		List<PkJumpSequenceIntegrity.TrajectoryPoint> active = Arrays.asList(
				point(821, "CURRENT", 0, 64, 0),
				point(822, "PENDING", 5, 64, 0)
		);
		PkJump.GuardedCandidates guarded = new PkJump.GuardedCandidates(
				Collections.singletonList(lateral),
				Collections.singletonList(lateral),
				0,
				false,
				Collections.singletonList(acceptedDiagnostic(lateral))
		);

		PkJump.GuardedCandidates result = PkJump.filterOneStepViableCandidates(
				guarded,
				Collections.emptyList(),
				active,
				Difficulty.EXPERT
		);

		assertTrue(result.candidates.contains(lateral));
		assertTrue(result.diagnostics.get(0).viableNextCandidates > 0);
	}

	@Test
	void lookaheadDoesNotMutateInputTrajectoryOrSpatialHistory() {
		Location candidate = loc(6, 64, 0);
		List<Location> spatialHistory = Collections.singletonList(loc(-3, 64, 0));
		List<PkJumpSequenceIntegrity.TrajectoryPoint> active = Arrays.asList(
				point(831, "CURRENT", 0, 64, 0),
				point(832, "PENDING", 3, 64, 0)
		);

		PkJump.viableNextCandidateCount(candidate, spatialHistory, active, Difficulty.EXPERT);

		assertEquals(1, spatialHistory.size());
		assertEquals(2, active.size());
		assertEquals(832, active.get(1).sequenceId);
	}

	private void assertReachable(int horizontal, int dy) {
		assertTrue(PkJumpSequenceIntegrity.canReach(loc(0, 64, 0), loc(horizontal, 64 + dy, 0), Difficulty.EXPERT), "h=" + horizontal + " dy=" + dy);
	}

	private void assertUnreachable(int horizontal, int dy) {
		assertFalse(PkJumpSequenceIntegrity.canReach(loc(0, 64, 0), loc(horizontal, 64 + dy, 0), Difficulty.EXPERT), "h=" + horizontal + " dy=" + dy);
	}

	private PkJump.CandidateDiagnostic acceptedDiagnostic(Location candidate) {
		PkJump.CandidateDiagnostic diagnostic = new PkJump.CandidateDiagnostic(candidate, 0, Double.POSITIVE_INFINITY, 0, true, true, true, PkJumpSequenceIntegrity.PASS);
		diagnostic.sequenceValidation = new PkJumpSequenceIntegrity.SequenceValidation(true, PkJumpSequenceIntegrity.PASS, null, Collections.emptyList());
		return diagnostic;
	}

	private PkJumpSequenceIntegrity.TrajectoryPoint point(long sequenceId, String role, int x, int y, int z) {
		return new PkJumpSequenceIntegrity.TrajectoryPoint(sequenceId, role, loc(x, y, z));
	}

	private Location loc(int x, int y, int z) {
		return new Location(world, x, y, z);
	}
}
