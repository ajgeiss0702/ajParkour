package us.ajg0702.parkour.game;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Skull;
import org.bukkit.metadata.FixedMetadataValue;

import us.ajg0702.parkour.Main;
import us.ajg0702.parkour.utils.MaterialParser;

public class PkJump {

	private static final AtomicLong GENERATION_IDS = new AtomicLong();

	private final long sequenceId;
	
	PkPlayer ply;
	Manager man;
	
	Main main;
	
	List<Location> blocks;
	
	boolean placed = false;

	/**
	 * Creating a jump for the player. Will calculate best possible direction to place the block.
	 * @param ply A {@link us.ajg0702.parkour.game.PkPlayer PkPlayer} that the block belongs to
	 * @param from The 'from' location of the previous jump
	 */
	public PkJump(PkPlayer ply, Location from) {
		sequenceId = GENERATION_IDS.incrementAndGet();
		man = ply.man;
		this.ply = ply;
		this.main = man.main;
		
		World w = from.getWorld();
		int x = from.getBlockX();
		int y = from.getBlockY();
		int z = from.getBlockZ();
		
		List<Location> bks = new ArrayList<>();

		Difficulty d = effectiveDifficulty(ply);

		long generationId = sequenceId;
		Location previous = ply.getPreviousMovementOrigin();
		List<Location> spatialHistory = guardedRecentReferences(ply, from);
		List<PkJumpSequenceIntegrity.TrajectoryPoint> activeTrajectory = ply.getActiveTrajectory();
		boolean guardEnabled = NayatsuGenerationConfig.antiUTurnGuardEnabled(main);
		GuardedCandidates guarded = null;
		int maxGeneratedY = maxGeneratedY(ply);
		bks = candidateUniverse(w, x, y, z, d, maxGeneratedY);
		guarded = guardEnabled ?
				filterGuardedCandidates(bks, previous, from, spatialHistory, activeTrajectory, d) :
				new GuardedCandidates(bks, bks, 0, false, Collections.emptyList());
		if(guarded == null || guarded.candidates.isEmpty()) {
			throw new IllegalStateException("NO_VALID_CANDIDATE: no sequence-safe and UX-guarded candidates survived the finite jumps.yml universe");
		}
		bks = guarded.candidates;
		HashMap<Object, Double> sc = new HashMap<>();
		for(Location bk : guarded.originalCandidates) {
			sc.put(bk, (double)getBlockScore(bk, from, ply.getArea(), ply, ply.getPlayer().getLocation().getYaw()));
		}
		if(NayatsuGenerationConfig.debugFallbackLogEnabled(main)) {
			logGenerationDiagnostics(generationId, from, previous, ply.getPlayer().getLocation().getYaw(), spatialHistory, activeTrajectory, guarded, sc);
		}
		
		/*for(Object k : sc.keySet()) {
			Location l = Location.deserialize(((Location) k).serialize());;
			l.setY(l.getY()+1);
			Double s = sc.get(k);
			l.getBlock().setType(Material.SIGN);
			if(!(l.getBlock().getState() instanceof Sign)) return;
			Sign sign = (Sign) l.getBlock().getState();
			sign.setLine(0, s+"");
			sign.update();
		}*/
		
		LinkedHashMap<Object, Double> scs;
		scs = main.sortByValueWithObjectKey(sc, false);
		
		
		
		Object[] scsk = scs.keySet().toArray();
		
		Double last = scs.get(scsk[scsk.length-1]);
		Object selected;
		
		Map<Object, Double> poss = selectHighestEligibleScores(scs, bks, last);

		List<Object> posskeys = new ArrayList<>(poss.keySet());
		
		//System.out.println(posskeys.size()+" o: " + poss.keySet().size());
		
		int ki = Main.random(0, posskeys.size()-1);
		selected = posskeys.get(ki);
		if(NayatsuGenerationConfig.debugFallbackLogEnabled(main)) {
			Bukkit.getLogger().info("[ajParkour] generation=" + generationId + " SELECTED candidate=" + xyz((Location) selected) + " eligibleCandidates=" + bks.size() + " rejectedCandidates=" + guarded.rejectedCount + " fallbackUsed=" + guarded.fallbackUsed + " score=" + sc.get(selected));
		}
		
		
		blocks = new ArrayList<>();
		blocks.add((Location) selected);
	}
	
	
	/**
	 * Calculates the score for a particular block location
	 * @param block a {@link org.bukkit.Location Location} of the block to get the score of
	 * @param from a {@link org.bukkit.Location Location} of the 'from' position of the previous jump
	 * @param area The PkArea the block would be in
	 * @param ply The player the block would belong to.
	 * @param yaw a float with the player's yaw (left-right looking)
	 * @return the score of a block. (usually 0-10, but can be higher or lower)
	 */
	public static int getBlockScore(Location block, Location from, PkArea area, PkPlayer ply, float yaw) {
		if(yaw < 0) {
			yaw +=360;
		}
		
		if(yaw > 180) {
			yaw -= 360;
			yaw = Math.abs(yaw)*-1;
		}
		yaw *= -1; // I did the things below backwards, so this is a quick fix.



		int score = 10;
		
		World world = block.getWorld();
		int x = block.getBlockX();
		int y = block.getBlockY();
		int z = block.getBlockZ();
		List<Location> shouldBeAir = Arrays.asList(
				block,
				new Location(world, x, y-1, z),
				new Location(world, x, y-2, z),
				new Location(world, x, y+3, z),
				new Location(world, x, y+1, z),
				new Location(world, x, y+2, z),
				new Location(world, x, y+3, z)
		);
		boolean returnNow = false;
		for(Location l : shouldBeAir) {
			if(l.getBlock().getType().equals(Material.AIR)) continue;
			returnNow = true;
			score -= 50;
		}
		if(returnNow) return score;
		
		float[] dirs = new float[5];
		dirs[0] = 0f; // +z
		//dirs[1] = 45f; // +z,-x
		dirs[1] = 90f; // -x
		//dirs[3] = 135f; // -x, -z
		dirs[2] = 180f; // -z
		dirs[3] = -180f;
		//dirs[5] = -135f; // -z +x
		dirs[4] = -90f; // +x
		
		//dirs[7] = -45f; // +x +Z
		
		int xc = from.getBlockX()-block.getBlockX();
		//int yc = from.getBlockY()-block.getBlockY();
		int zc = from.getBlockZ()-block.getBlockZ();
		
		
		//ply.getPlayer().sendMessage(ply.msgs.color("&9----------------"));
		float closest;
		float distance = Math.abs(dirs[0] - yaw);
		int idx = 0;
		for(int c = 1; c < dirs.length; c++){
		    float cdistance = Math.abs(dirs[c] - yaw);
		    //ply.getPlayer().sendMessage(dirs[c]+": "+cdistance + ply.msgs.color(" &7 | ")+dirs[c]+" - "+yaw);
		    if(cdistance < distance){
		        idx = c;
		        distance = cdistance;
		        //ply.getPlayer().sendMessage(ply.msgs.color("&a^"));
		    }
		}
		closest = dirs[idx];
		//ply.getPlayer().sendMessage(ply.msgs.color("&eClosest: " + closest+"\n&9-----------------"));

		int goodr = Main.random(-10, 1);
		
		if(pos(zc) && zero(xc)) {
			if(sfloatEquals(closest, 180f)) {
				score += goodr;
			} else if(sfloatEquals(closest, 135f) || sfloatEquals(closest, -135f)) {
				score += 1;
			} else if(sfloatEquals(closest, -90f) || sfloatEquals(closest, 90f)) {
				score -= 3;
			} else {
				score -= 7;
			}
			
		} else if(pos(zc) && neg(xc)) {
			if(floatEquals(closest, 45f)) {
				score += goodr;
			} else if(sfloatEquals(closest, 90f) || sfloatEquals(closest, 0f)) {
				score += 1;
			} else if(sfloatEquals(closest, 135f) || sfloatEquals(closest, -45f)) {
				score -= 3;
			} else {
				score -= 7;
			}
		} else if(neg(xc) && zero(zc)) {
			if(floatEquals(closest, 90f)) {
				score += goodr;
			} else if(sfloatEquals(closest, 45f) || sfloatEquals(closest, 135f)) {
				score += 1;
			} else if(sfloatEquals(closest, 0f) || sfloatEquals(closest, 180f)) {
				score -= 3;
			} else {
				score -= 7;
			}
		} else if(neg(xc) && neg(zc)) {
			if(floatEquals(closest, 135f)) {
				score += goodr;
			} else if(sfloatEquals(closest, 90f) || sfloatEquals(closest, 180f)) {
				score += 1;
			} else if(sfloatEquals(closest, -135f) || sfloatEquals(closest, 45f)) {
				score -= 3;
			} else {
				score -= 7;
			}
		} else if(neg(zc) && zero(xc)) {
			if(floatEquals(closest, 0f)) {
				score += goodr;
			} else if(sfloatEquals(closest, 45f) || sfloatEquals(closest, -45f)) {
				score += 1;
			} else if(sfloatEquals(closest, 90f) || sfloatEquals(closest, -90f)) {
				score -= 3;
			} else {
				score -= 7;
			}
		} else if(pos(xc) && neg(zc)) {
			if(floatEquals(closest, -135f)) {
				score += goodr;
			} else if(sfloatEquals(closest, 180f) || sfloatEquals(closest, -90f)) {
				score += 1;
			} else if(sfloatEquals(closest, 135f) || sfloatEquals(closest, -45f)) {
				score -= 3;
			} else {
				score -= 7;
			}
		} else if(pos(xc) && zero(zc)) {
			if(floatEquals(closest, -90f)) {
				score += goodr;
			} else if(sfloatEquals(closest, -45f) || sfloatEquals(closest, -135f)) {
				score += 1;
			} else if(sfloatEquals(closest, 0f) || sfloatEquals(closest, 180f)) {
				score -= 3;
			} else {
				score -= 7;
			}
		} else if(pos(xc) && pos(zc)) {
			if(floatEquals(closest, -45f)) {
				score += goodr;
			} else if(sfloatEquals(closest, 0f) || sfloatEquals(closest, -90f)) {
				score += 1;
			} else if(sfloatEquals(closest, -135f) || sfloatEquals(closest, 45f)) {
				score -= 3;
			} else {
				score -= 7;
			}
		} else {
			Bukkit.getLogger().warning("[ajParkour] Could not find direction for jump score!");
		}
		
		//Bukkit.broadcastMessage("After direction: "+score);
		
		List<Double> ds = new ArrayList<>();
		for(PkPlayer p : Manager.getInstance().getPlayersInArea(area)) {
			if(ply != null) {
				if(p.equals(ply)) continue;
			}
			List<Double> l = new ArrayList<>();
			for(PkJump j : p.jumps) {
				Location t = j.getFrom();
				l.add(block.distance(t));
			}
			Collections.sort(l);
			ds.add(l.get(0));
		}
		if(ds.size() > 0) {
			Collections.sort(ds);
			long ch = 7-Math.round(ds.get(0));
			score -= (ch > 0) ? ch : 0;
		}
		
		//Bukkit.broadcastMessage("After players: "+score);
		
		
		int d = (int) Math.round(area.distanceFromWall(block));
		
		if(!area.contains(block)) {
			d = Math.abs(d) * -1;
			score -= 10;
		}
		//Bukkit.broadcastMessage("d obounds: "+d);
		
		if(d <= 0) {
			d -= 10;
		}
		//Bukkit.broadcastMessage("d ebounds: "+d);
		
		if(d < 7 && d >= 0) {
			score -= 7-d;
		} else if(d < 0) {
			score += d;
		}
		//Bukkit.broadcastMessage("After bounds: "+score);
	
		
		return score;
	}

	static JumpShape shapeForDistance(int r) {
		int maxy = 1;
		if(r > 4) {
			maxy = 0;
		}
		if(r >= 5) {
			r = 5;
			maxy = 0;
		}
		return new JumpShape(r, maxy);
	}

	static Difficulty effectiveDifficulty(PkPlayer ply) {
		Difficulty d = ply.getArea().getDifficulty();
		JumpManager jm = JumpManager.getInstance();

		if(d.equals(Difficulty.BALANCED)) {
			d = Difficulty.EASY;
			if(ply.getScore() >= jm.getBalancedStart(Difficulty.MEDIUM)) {
				d = Difficulty.MEDIUM;
			}
			if(ply.getScore() >= jm.getBalancedStart(Difficulty.HARD)) {
				d = Difficulty.HARD;
			}
			if(ply.getScore() >= jm.getBalancedStart(Difficulty.EXPERT)) {
				d = Difficulty.EXPERT;
			}
		}
		return d;
	}

	static int maxGeneratedY(PkPlayer ply) {
		if(ply.getJumps().size() >= 2) {
			int prevy = ply.getJumps().get(ply.jumps.size()-1).getFrom().getBlockY();
			int prev2y = ply.getJumps().get(ply.jumps.size()-2).getFrom().getBlockY();
			if(prevy - prev2y > 0) {
				return 0;
			}
		}
		return 1;
	}

	static List<Location> candidateUniverse(World w, int x, int y, int z, Difficulty difficulty, int maxGeneratedY) {
		List<Location> bks = new ArrayList<>();
		for(int rawDistance = difficulty.getMin(); rawDistance <= difficulty.getMax(); rawDistance++) {
			JumpShape shape = shapeForDistance(rawDistance);
			int maxy = Math.min(shape.maxY, maxGeneratedY);
			for(Location candidate : candidateLocations(w, x, y, z, shape.distance, maxy)) {
				if(!containsSameBlock(bks, candidate)) {
					bks.add(candidate);
				}
			}
		}
		return bks;
	}

	static List<Location> candidateLocations(World w, int x, int y, int z, int r, int maxy) {
		List<Location> bks = new ArrayList<>();
		bks.add(new Location(w, x+r, y, z));
		bks.add(new Location(w, x-r, y, z));
		bks.add(new Location(w, x+r, y+maxy, z));
		bks.add(new Location(w, x-r, y+maxy, z));
		bks.add(new Location(w, x+r, y-maxy, z));
		bks.add(new Location(w, x-r, y-maxy, z));
		bks.add(new Location(w, x, y+maxy, z+r));
		bks.add(new Location(w, x, y-maxy, z+r));
		bks.add(new Location(w, x, y+maxy, z-r));
		bks.add(new Location(w, x, y-maxy, z-r));
		bks.add(new Location(w, x, y+maxy, z+r));
		bks.add(new Location(w, x, y, z+r));
		bks.add(new Location(w, x, y, z-r));
		return bks;
	}

	private static boolean containsSameBlock(List<Location> locations, Location candidate) {
		for(Location location : locations) {
			if(sameBlock(location, candidate)) {
				return true;
			}
		}
		return false;
	}

	static GuardedCandidates filterReverseTurnCandidates(List<Location> candidates, Location previous, Location from) {
		return filterNayatsuUxGuardCandidates(candidates, previous, from, Collections.emptyList());
	}

	static GuardedCandidates filterGuardedCandidates(List<Location> candidates, Location previous, Location from, List<Location> spatialHistory, List<PkJumpSequenceIntegrity.TrajectoryPoint> activeTrajectory, Difficulty difficulty) {
		List<CandidateDiagnostic> diagnostics = new ArrayList<>();
		List<Location> kept = new ArrayList<>();
		for(Location candidate : candidates) {
			CandidateDiagnostic diagnostic = evaluateNayatsuUxGuard(candidate, previous, from, spatialHistory);
			diagnostic.sequenceValidation = PkJumpSequenceIntegrity.validateAppend(activeTrajectory, from, candidate, difficulty);
			diagnostic.accepted = diagnostic.accepted && diagnostic.sequenceValidation.accepted;
			if(!diagnostic.sequenceValidation.accepted) {
				diagnostic.reason = diagnostic.sequenceValidation.reason;
			}
			diagnostics.add(diagnostic);
			if(diagnostic.accepted) {
				kept.add(candidate);
			}
		}
		return new GuardedCandidates(candidates, kept, candidates.size() - kept.size(), false, diagnostics);
	}

	static GuardedCandidates filterNayatsuUxGuardCandidates(List<Location> candidates, Location previous, Location from, List<Location> recentReferences) {
		List<CandidateDiagnostic> diagnostics = new ArrayList<>();
		List<Location> kept = new ArrayList<>();
		for(Location candidate : candidates) {
			CandidateDiagnostic diagnostic = evaluateNayatsuUxGuard(candidate, previous, from, recentReferences);
			diagnostics.add(diagnostic);
			if(diagnostic.accepted) {
				kept.add(candidate);
			}
		}
		return new GuardedCandidates(candidates, kept, candidates.size() - kept.size(), false, diagnostics);
	}

	static CandidateDiagnostic evaluateNayatsuUxGuard(Location candidate, Location previous, Location from, List<Location> recentReferences) {
		int prevX = previous == null ? 0 : Integer.compare(from.getBlockX() - previous.getBlockX(), 0);
		int prevZ = previous == null ? 0 : Integer.compare(from.getBlockZ() - previous.getBlockZ(), 0);
		int nextX = Integer.compare(candidate.getBlockX() - from.getBlockX(), 0);
		int nextZ = Integer.compare(candidate.getBlockZ() - from.getBlockZ(), 0);
		double distanceFromCurrent = horizontalDistance(candidate, from);
		double nearestRecentDistance = nearestHorizontalDistance(candidate, recentReferences);
		boolean hasPreviousDirection = previous != null && !(prevX == 0 && prevZ == 0);
		boolean reverse = hasPreviousDirection && nextX == -prevX && nextZ == -prevZ;
		boolean recentRegion = nearestRecentDistance <= distanceFromCurrent;
		boolean accepted = !reverse && !recentRegion;
		String reason = "PASS";
		if(reverse) {
			reason = "ANTI_U_TURN";
		} else if(recentRegion) {
			reason = "RECENT_REGION";
		}
		return new CandidateDiagnostic(candidate, distanceFromCurrent, nearestRecentDistance, turnAngle(previous, from, candidate), !reverse, !recentRegion, accepted, reason);
	}

	static List<Location> guardedRecentReferences(PkPlayer ply, Location from) {
		List<Location> references = new ArrayList<>();
		references.addAll(ply.getSpatialHistory());
		return references;
	}

	private static double nearestHorizontalDistance(Location candidate, List<Location> references) {
		double nearest = Double.POSITIVE_INFINITY;
		for(Location reference : references) {
			if(reference == null || candidate.getWorld() != null && reference.getWorld() != null && !candidate.getWorld().equals(reference.getWorld())) continue;
			nearest = Math.min(nearest, horizontalDistance(candidate, reference));
		}
		return nearest;
	}

	private static double horizontalDistance(Location a, Location b) {
		double dx = a.getBlockX() - b.getBlockX();
		double dz = a.getBlockZ() - b.getBlockZ();
		return Math.sqrt(dx * dx + dz * dz);
	}

	private static double turnAngle(Location previous, Location from, Location candidate) {
		if(previous == null) return 0;
		double ax = from.getBlockX() - previous.getBlockX();
		double az = from.getBlockZ() - previous.getBlockZ();
		double bx = candidate.getBlockX() - from.getBlockX();
		double bz = candidate.getBlockZ() - from.getBlockZ();
		double amag = Math.sqrt(ax * ax + az * az);
		double bmag = Math.sqrt(bx * bx + bz * bz);
		if(amag == 0 || bmag == 0) return 0;
		double dot = ax * bx + az * bz;
		double cosine = Math.max(-1, Math.min(1, dot / (amag * bmag)));
		return Math.toDegrees(Math.acos(cosine));
	}

	private static boolean sameBlock(Location a, Location b) {
		if(a == null || b == null) return false;
		if(a.getWorld() != null && b.getWorld() != null && !a.getWorld().equals(b.getWorld())) return false;
		return
				a.getBlockX() == b.getBlockX() &&
				a.getBlockY() == b.getBlockY() &&
				a.getBlockZ() == b.getBlockZ();
	}

	static Map<Object, Double> selectHighestEligibleScores(Map<Object, Double> sortedScores, List<Location> eligibleCandidates, Double globalBestScore) {
		Map<Object, Double> poss = new HashMap<>();
		for(Object key : sortedScores.keySet()) {
			if(!eligibleCandidates.contains(key)) continue;
			Double v = sortedScores.get(key);
			if(Math.abs(v - globalBestScore) < 0.0001) {
				poss.put(key, v);
			}
		}
		if(!poss.isEmpty()) {
			return poss;
		}

		Double eligibleBest = null;
		for(Object key : sortedScores.keySet()) {
			if(!eligibleCandidates.contains(key)) continue;
			Double v = sortedScores.get(key);
			if(eligibleBest == null || v > eligibleBest) {
				eligibleBest = v;
			}
		}
		if(eligibleBest == null) {
			return poss;
		}
		for(Object key : sortedScores.keySet()) {
			if(!eligibleCandidates.contains(key)) continue;
			Double v = sortedScores.get(key);
			if(Math.abs(v - eligibleBest) < 0.0001) {
				poss.put(key, v);
			}
		}
		return poss;
	}

	private static void logGenerationDiagnostics(long generationId, Location from, Location previous, float yaw, List<Location> recentReferences, List<PkJumpSequenceIntegrity.TrajectoryPoint> activeTrajectory, GuardedCandidates guarded, Map<Object, Double> scores) {
		Bukkit.getLogger().info("[ajParkour] generation=" + generationId + " candidateSequence=#" + generationId + " current=" + xyz(from) + " previous=" + xyz(previous) + " movementVector=" + vector(previous, from) + " playerYaw=" + yaw + " spatialHistory=" + xyzList(recentReferences) + " activeTrajectory=" + trajectoryList(activeTrajectory));
		for(CandidateDiagnostic diagnostic : guarded.diagnostics) {
			Double score = scores.get(diagnostic.candidate);
			Bukkit.getLogger().info("[ajParkour] generation=" + generationId +
					" candidate=" + xyz(diagnostic.candidate) +
					" distanceFromCurrent=" + diagnostic.distanceFromCurrent +
					" nearestRecentDistance=" + diagnostic.nearestRecentDistance +
					" turnAngle=" + diagnostic.turnAngle +
					" yawDelta=NA" +
					" antiUTurn=" + passReject(diagnostic.antiUTurnPass) +
					" recentRegion=" + passReject(diagnostic.recentRegionPass) +
					" visualSeparation=" + passReject(diagnostic.accepted) +
					" originalEligibility=PASS" +
					" final=" + (diagnostic.accepted ? "ACCEPT" : "REJECT") +
					" reason=" + diagnostic.reason +
					" score=" + score +
					" reachability=" + reachabilityList(diagnostic.sequenceValidation));
		}
	}

	private static String passReject(boolean pass) {
		return pass ? "PASS" : "REJECT";
	}

	private static String xyz(Location location) {
		if(location == null) return "null";
		return location.getWorld().getName() + ":" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
	}

	private static String vector(Location previous, Location current) {
		if(previous == null || current == null) return "null";
		return (current.getBlockX() - previous.getBlockX()) + "," + (current.getBlockY() - previous.getBlockY()) + "," + (current.getBlockZ() - previous.getBlockZ());
	}

	private static String xyzList(List<Location> locations) {
		List<String> raw = new ArrayList<>();
		for(Location location : locations) {
			raw.add(xyz(location));
		}
		return raw.toString();
	}

	private static String trajectoryList(List<PkJumpSequenceIntegrity.TrajectoryPoint> trajectory) {
		List<String> raw = new ArrayList<>();
		for(PkJumpSequenceIntegrity.TrajectoryPoint point : trajectory) {
			raw.add("#" + point.sequenceId + " " + point.role + " " + xyz(point.location));
		}
		return raw.toString();
	}

	private static String reachabilityList(PkJumpSequenceIntegrity.SequenceValidation validation) {
		if(validation == null) return "[]";
		List<String> raw = new ArrayList<>();
		for(PkJumpSequenceIntegrity.ReachabilityEdge edge : validation.edges) {
			raw.add("#" + edge.from.sequenceId + "->candidate=" + edge.reachable + (edge.label.isEmpty() ? "" : " " + edge.label));
		}
		if(validation.source != null) {
			raw.add("shortcutSource=#" + validation.source.sequenceId);
		}
		return raw.toString();
	}

	static class GuardedCandidates {
		final List<Location> originalCandidates;
		final List<Location> candidates;
		final int rejectedCount;
		final boolean fallbackUsed;
		final List<CandidateDiagnostic> diagnostics;

		GuardedCandidates(List<Location> candidates, boolean fallbackUsed) {
			this(candidates, candidates, 0, fallbackUsed, Collections.emptyList());
		}

		GuardedCandidates(List<Location> originalCandidates, List<Location> candidates, int rejectedCount, boolean fallbackUsed, List<CandidateDiagnostic> diagnostics) {
			this.originalCandidates = originalCandidates;
			this.candidates = candidates;
			this.rejectedCount = rejectedCount;
			this.fallbackUsed = fallbackUsed;
			this.diagnostics = diagnostics;
		}
	}

	static class CandidateDiagnostic {
		final Location candidate;
		final double distanceFromCurrent;
		final double nearestRecentDistance;
		final double turnAngle;
		final boolean antiUTurnPass;
		final boolean recentRegionPass;
		boolean accepted;
		String reason;
		PkJumpSequenceIntegrity.SequenceValidation sequenceValidation;

		CandidateDiagnostic(Location candidate, double distanceFromCurrent, double nearestRecentDistance, double turnAngle, boolean antiUTurnPass, boolean recentRegionPass, boolean accepted, String reason) {
			this.candidate = candidate;
			this.distanceFromCurrent = distanceFromCurrent;
			this.nearestRecentDistance = nearestRecentDistance;
			this.turnAngle = turnAngle;
			this.antiUTurnPass = antiUTurnPass;
			this.recentRegionPass = recentRegionPass;
			this.accepted = accepted;
			this.reason = reason;
		}
	}

	static class JumpShape {
		final int distance;
		final int maxY;

		JumpShape(int distance, int maxY) {
			this.distance = distance;
			this.maxY = maxY;
		}
	}
	
	
	
	/**
	 * Get 'from' location
	 * @return a {@link org.bukkit.Location Location} that the player is supposed to jump from this jump to the next one
	 */
	public Location getFrom() {
		Location r = blocks.get(blocks.size()-1);
		if(r == null) Bukkit.getLogger().warning("[ajParkour] Warning: getFrom() returned null!");
		return r;
	}

	long getSequenceId() {
		return sequenceId;
	}

	/**
	 * Get 'to' location
	 * @return a {@link org.bukkit.Location Location} that the player is supposed to jump to from the previous jump
	 */
	public Location getTo() {
		return blocks.get(0);
	}
	
	
	/**
	 * Places all blocks for this jump
	 */
	public void place() {
		for(Location l : blocks) {
			Material prev = l.getBlock().getType();
			String type = ply.getBlock();
			if(((String) main.config.get("random-block-selection")).equalsIgnoreCase("each")) {
				type = main.selector.getBlock(ply.getPlayer(), ply.getArea());
			}
			MaterialParser.placeBlock(l, type);
			l.getBlock().setMetadata("ajpk-prevtype", new FixedMetadataValue(main, prev));
			if(type.equalsIgnoreCase("SKULL") || type.equalsIgnoreCase("PLAYER_HEAD")) {
				List<UUID> presents = main.selector.getPresents();
				Skull sd = (Skull) l.getBlock().getState();
				UUID id = presents.get(Main.random(0, presents.size()-1));
				OfflinePlayer p = Bukkit.getOfflinePlayer(id);
				ply.ply.sendMessage(p.getName());
				sd.setOwningPlayer(p);
				sd.update();
			}
		}
		placed = true;
	}
	
	public boolean isPlaced() {
		return placed;
	}
	
	/*
	public static GameProfile getNonPlayerProfile(String skinURL, boolean randomName) {
		GameProfile newSkinProfile = new GameProfile(UUID.randomUUID(), randomName ? getRandomString(16) : null);
		newSkinProfile.getProperties().put("textures", new Property("textures", Base64Coder.encodeString("{textures:{SKIN:{url:\"" + skinURL + "\"}}}")));
		return newSkinProfile;
		}
	public static void setSkullWithNonPlayerProfile(String skinURL, boolean randomName, Block skull) {
		if(skull.getType() != Material.SKULL)
		throw new IllegalArgumentException("Block must be a skull.");
		TileEntitySkull skullTile = (TileEntitySkull)((CraftWorld)skull.getWorld()).getHandle().getTileEntity(skull.getX(), skull.getY(), skull.getZ());
		skullTile.setGameProfile(getNonPlayerProfile(skinURL, randomName));
		skull.getWorld().refreshChunk(skull.getChunk().getX(), skull.getChunk().getZ());
		}*/
	

	/**
	 * Breaks all blocks from this jump
	 */
	public void remove() {
		for(Location l : blocks) {
			//List<MetadataValue> metaDataValues = l.getBlock().getMetadata("PlacedBlock");
			//Material prev = null;
			//for (MetadataValue value : metaDataValues) {
		    //    prev = (Material) value.value();
		    //}
			//if(prev != null) {
			//	l.getBlock().setType(prev);
			//} else {
				l.getBlock().setType(Material.AIR);
			//}
		}
		placed = false;
	}
	
	
	private static boolean pos(int x) {
		return x > 0;
	}
	
	private static boolean neg(int x) {
		return x < 0;
	}
	private static boolean zero(int x) {
		return x == 0;
	}
	private static boolean sfloatEquals(float o, float t) {
		float d = Math.abs(o - t);
		if(floatEquals(t, 180f)) {
			if(floatEquals(o, -180f)) {
				return true;
			}
		}
		return d < 0.001;
	}
	private static boolean floatEquals(float o, float t) {
		return Math.abs(o - t) < 0.001;
	}
	
	
	private static int random(int min, int max) {


		if (min > max) {
			throw new IllegalArgumentException("max must be greater than min: "+min+"-"+max);
		} else if(min == max) {
			return min;
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}


}
