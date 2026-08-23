package us.ajg0702.parkour.game;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class PkJumpSequenceIntegrity {

	static final String PASS = "PASS";
	static final String NEXT_NOT_REACHABLE = "REJECT_NEXT_NOT_REACHABLE";
	static final String NON_ADJACENT_SHORTCUT = "REJECT_NON_ADJACENT_SHORTCUT";
	static final String DUPLICATE_ACTIVE_PLATFORM = "REJECT_DUPLICATE_ACTIVE_PLATFORM";

	private PkJumpSequenceIntegrity() { }

	static boolean canReach(Location from, Location to, Difficulty difficulty) {
		if(from == null || to == null) return false;
		if(from.getWorld() != null && to.getWorld() != null && !from.getWorld().equals(to.getWorld())) return false;

		double horizontal = horizontalDistance(from, to);
		int vertical = to.getBlockY() - from.getBlockY();
		if(horizontal <= 0) return false;

		int generatedMax = PkJump.shapeForDistance(difficulty.getMax()).distance;
		int downwardBonus = vertical < 0 ? Math.min(2, Math.abs(vertical)) : 0;
		int maxHorizontal = generatedMax + downwardBonus;
		if(horizontal - maxHorizontal > 0.0001) return false;

		if(vertical > 1) return false;
		return vertical <= 0 || horizontal <= 4.0001;
	}

	static SequenceValidation validateAppend(List<TrajectoryPoint> activeTrajectory, Location fallbackPredecessor, Location candidate, Difficulty difficulty) {
		List<TrajectoryPoint> trajectory = activeTrajectory == null ? Collections.emptyList() : activeTrajectory;
		TrajectoryPoint predecessor = trajectory.isEmpty() ?
				new TrajectoryPoint(-1, "CURRENT", fallbackPredecessor) :
				trajectory.get(trajectory.size() - 1);

		List<ReachabilityEdge> edges = new ArrayList<>();
		for(TrajectoryPoint point : trajectory) {
			if(sameBlock(point.location, candidate)) {
				return new SequenceValidation(false, DUPLICATE_ACTIVE_PLATFORM, point, edges);
			}
		}

		boolean nextReachable = canReach(predecessor.location, candidate, difficulty);
		edges.add(new ReachabilityEdge(predecessor, nextReachable, "ADJACENT_OK"));
		if(!nextReachable) {
			return new SequenceValidation(false, NEXT_NOT_REACHABLE, predecessor, edges);
		}

		for(int i = 0; i < trajectory.size() - 1; i++) {
			TrajectoryPoint point = trajectory.get(i);
			boolean reachable = canReach(point.location, candidate, difficulty);
			edges.add(new ReachabilityEdge(point, reachable, reachable ? NON_ADJACENT_SHORTCUT : ""));
			if(reachable) {
				return new SequenceValidation(false, NON_ADJACENT_SHORTCUT, point, edges);
			}
		}

		return new SequenceValidation(true, PASS, null, edges);
	}

	static SequenceValidation validateFullTrajectory(List<TrajectoryPoint> activeTrajectory, Difficulty difficulty) {
		List<TrajectoryPoint> trajectory = activeTrajectory == null ? Collections.emptyList() : activeTrajectory;
		List<ReachabilityEdge> edges = new ArrayList<>();
		for(int i = 0; i < trajectory.size(); i++) {
			for(int j = i + 1; j < trajectory.size(); j++) {
				TrajectoryPoint from = trajectory.get(i);
				TrajectoryPoint to = trajectory.get(j);
				boolean reachable = canReach(from.location, to.location, difficulty);
				if(j == i + 1) {
					edges.add(new ReachabilityEdge(from, reachable, reachable ? "ADJACENT_OK" : NEXT_NOT_REACHABLE));
					if(!reachable) {
						return new SequenceValidation(false, NEXT_NOT_REACHABLE, from, edges);
					}
				} else {
					edges.add(new ReachabilityEdge(from, reachable, reachable ? NON_ADJACENT_SHORTCUT : ""));
					if(reachable) {
						return new SequenceValidation(false, NON_ADJACENT_SHORTCUT, from, edges);
					}
				}
			}
		}
		return new SequenceValidation(true, PASS, null, edges);
	}

	private static double horizontalDistance(Location a, Location b) {
		double dx = a.getBlockX() - b.getBlockX();
		double dz = a.getBlockZ() - b.getBlockZ();
		return Math.sqrt(dx * dx + dz * dz);
	}

	static boolean sameBlock(Location a, Location b) {
		if(a == null || b == null) return false;
		if(a.getWorld() != null && b.getWorld() != null && !a.getWorld().equals(b.getWorld())) return false;
		return a.getBlockX() == b.getBlockX() &&
				a.getBlockY() == b.getBlockY() &&
				a.getBlockZ() == b.getBlockZ();
	}

	static class TrajectoryPoint {
		final long sequenceId;
		final String role;
		final Location location;

		TrajectoryPoint(long sequenceId, String role, Location location) {
			this.sequenceId = sequenceId;
			this.role = role;
			this.location = location;
		}
	}

	static class ReachabilityEdge {
		final TrajectoryPoint from;
		final boolean reachable;
		final String label;

		ReachabilityEdge(TrajectoryPoint from, boolean reachable, String label) {
			this.from = from;
			this.reachable = reachable;
			this.label = label;
		}
	}

	static class SequenceValidation {
		final boolean accepted;
		final String reason;
		final TrajectoryPoint source;
		final List<ReachabilityEdge> edges;

		SequenceValidation(boolean accepted, String reason, TrajectoryPoint source, List<ReachabilityEdge> edges) {
			this.accepted = accepted;
			this.reason = reason;
			this.source = source;
			this.edges = edges;
		}
	}
}
