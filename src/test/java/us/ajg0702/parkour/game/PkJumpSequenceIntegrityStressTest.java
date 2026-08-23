package us.ajg0702.parkour.game;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PkJumpSequenceIntegrityStressTest {

	private final World world = mock(World.class);

	@Test
	void deterministicSequenceIntegritySoak() throws IOException {
		Difficulty.EXPERT.setMin(2);
		Difficulty.EXPERT.setMax(5);

		long seed = 87234191L;
		int targetTransitions = 100000;
		long start = System.nanoTime();
		StressResult before = runSimulation(targetTransitions, seed, false);
		StressResult after = runSimulation(targetTransitions, seed, true);
		long runtimeNanos = System.nanoTime() - start;

		assertEquals(0, after.shortcutInvariantViolations);
		assertEquals(targetTransitions, after.generatedTransitions);
		assertTrue(after.deadEndCandidateRejects > 0);
		assertTrue(after.averageCandidateUniverseSize() > 0);

		Path report = Paths.get("build", "reports", "nayatsu-parkour", "slice-08-sequence-stress.md");
		Files.createDirectories(report.getParent());
		Files.write(report, stressReport(seed, before, after, runtimeNanos).getBytes(StandardCharsets.UTF_8));
	}

	private StressResult runSimulation(int targetTransitions, long seed, boolean useViability) {
		Random random = new Random(seed);
		StressResult result = new StressResult();
		List<PkJumpSequenceIntegrity.TrajectoryPoint> active = initialTrajectory();
		Deque<Location> spatialHistory = new ArrayDeque<>();

		while(result.generatedTransitions < targetTransitions) {
			Location from = active.get(active.size() - 1).location;
			Location previous = active.size() >= 2 ? active.get(active.size() - 2).location : null;
			List<Location> history = new ArrayList<>(spatialHistory);
			List<Location> universe = PkJump.candidateUniverse(world, from.getBlockX(), from.getBlockY(), from.getBlockZ(), Difficulty.EXPERT, previous != null && from.getBlockY() - previous.getBlockY() > 0 ? 0 : 1);
			result.totalCandidateUniverse += universe.size();
			result.maxCandidateEvaluationCount = Math.max(result.maxCandidateEvaluationCount, universe.size());

			PkJump.GuardedCandidates guarded = PkJump.filterGuardedCandidates(universe, previous, from, history, active, Difficulty.EXPERT);
			result.sequenceIntegrityRejects += countRejected(guarded, PkJumpSequenceIntegrity.NON_ADJACENT_SHORTCUT);
			result.recentRegionRejects += countRejected(guarded, "REJECT_RECENT_REGION");
			result.antiUTurnRejects += countRejected(guarded, "REJECT_ANTI_U_TURN");
			result.totalSurvivingBeforeViability += guarded.candidates.size();
			result.totalGenerations++;

			if(guarded.candidates.isEmpty()) {
				result.noValidCandidateCount++;
				active = initialTrajectory();
				spatialHistory.clear();
				continue;
			}

			PkJump.GuardedCandidates selectable = guarded;
			if(useViability) {
				selectable = PkJump.filterOneStepViableCandidates(guarded, history, active, Difficulty.EXPERT);
				result.deadEndCandidateRejects += countRejected(selectable, "REJECT_DEAD_END_CANDIDATE");
				result.totalSurvivingAfterViability += selectable.candidates.size();
			}

			if(selectable.candidates.isEmpty()) {
				result.onlyDeadEndCandidatesCount++;
				active = initialTrajectory();
				spatialHistory.clear();
				continue;
			}

			Location selected = selectable.candidates.get(random.nextInt(selectable.candidates.size()));
			if(!useViability && PkJump.viableNextCandidateCount(selected, history, active, Difficulty.EXPERT) == 0) {
				result.deadEndTerminations++;
				active = initialTrajectory();
				spatialHistory.clear();
				continue;
			}

			if(active.size() > 1) {
				spatialHistory.addLast(active.get(1).location);
				while(spatialHistory.size() > 5) {
					spatialHistory.removeFirst();
				}
			}
			active = PkJump.trajectoryAfterCurrentCompletion(active, selected);
			PkJumpSequenceIntegrity.SequenceValidation full = PkJumpSequenceIntegrity.validateFullTrajectory(active, Difficulty.EXPERT);
			if(!full.accepted) {
				result.shortcutInvariantViolations++;
			}
			result.generatedTransitions++;
		}
		return result;
	}

	private int countRejected(PkJump.GuardedCandidates guarded, String reason) {
		int count = 0;
		for(PkJump.CandidateDiagnostic diagnostic : guarded.diagnostics) {
			if(!diagnostic.accepted && reason.equals(diagnostic.reason)) {
				count++;
			}
		}
		return count;
	}

	private List<PkJumpSequenceIntegrity.TrajectoryPoint> initialTrajectory() {
		List<PkJumpSequenceIntegrity.TrajectoryPoint> active = new ArrayList<>();
		active.add(point(1, "CURRENT", 0, 64, 0));
		active.add(point(2, "PENDING", 5, 64, 0));
		return active;
	}

	private PkJumpSequenceIntegrity.TrajectoryPoint point(long sequenceId, String role, int x, int y, int z) {
		return new PkJumpSequenceIntegrity.TrajectoryPoint(sequenceId, role, new Location(world, x, y, z));
	}

	private String stressReport(long seed, StressResult before, StressResult after, long runtimeNanos) {
		double reduction = before.deadEndTerminations == 0 ? 100.0 :
				((before.deadEndTerminations - after.deadEndTerminations) * 100.0) / before.deadEndTerminations;
		return "# Slice 8 Sequence Integrity Stress\n\n" +
				"- seed: " + seed + "\n" +
				"- generated_transitions: " + after.generatedTransitions + "\n" +
				"- shortcut_invariant_violations: " + after.shortcutInvariantViolations + "\n" +
				"- no_valid_candidate_count: " + after.noValidCandidateCount + "\n" +
				"- only_dead_end_candidates_count: " + after.onlyDeadEndCandidatesCount + "\n" +
				"- dead_end_candidate_rejects: " + after.deadEndCandidateRejects + "\n" +
				"- dead_end_terminations_before_viability: " + before.deadEndTerminations + "\n" +
				"- dead_end_terminations_after_viability: " + after.deadEndTerminations + "\n" +
				"- dead_end_reduction_percent: " + reduction + "\n" +
				"- sequence_integrity_rejects: " + after.sequenceIntegrityRejects + "\n" +
				"- recent_region_rejects: " + after.recentRegionRejects + "\n" +
				"- anti_u_turn_rejects: " + after.antiUTurnRejects + "\n" +
				"- average_candidate_universe_size: " + after.averageCandidateUniverseSize() + "\n" +
				"- average_surviving_before_viability: " + after.averageSurvivingBeforeViability() + "\n" +
				"- average_surviving_after_viability: " + after.averageSurvivingAfterViability() + "\n" +
				"- max_candidate_evaluation_count: " + after.maxCandidateEvaluationCount + "\n" +
				"- runtime_ms: " + (runtimeNanos / 1_000_000.0) + "\n";
	}

	private static class StressResult {
		int generatedTransitions;
		int totalGenerations;
		int shortcutInvariantViolations;
		int noValidCandidateCount;
		int onlyDeadEndCandidatesCount;
		int deadEndCandidateRejects;
		int deadEndTerminations;
		int sequenceIntegrityRejects;
		int recentRegionRejects;
		int antiUTurnRejects;
		int maxCandidateEvaluationCount;
		long totalCandidateUniverse;
		long totalSurvivingBeforeViability;
		long totalSurvivingAfterViability;

		double averageCandidateUniverseSize() {
			return totalGenerations == 0 ? 0 : totalCandidateUniverse / (double) totalGenerations;
		}

		double averageSurvivingBeforeViability() {
			return totalGenerations == 0 ? 0 : totalSurvivingBeforeViability / (double) totalGenerations;
		}

		double averageSurvivingAfterViability() {
			return totalGenerations == 0 ? 0 : totalSurvivingAfterViability / (double) totalGenerations;
		}
	}
}
