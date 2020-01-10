package us.ajg0702.parkour.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

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
	
	PkPlayer ply;
	Manager man;
	
	Main main;
	
	List<Location> blocks;
	
	boolean placed = false;

	/**
	 * Creating a jump for the payer. Will calculate best possible direction to place the block.
	 * @param ply A {@link us.ajg0702.parkour.game.PkPlayer PkPlayer} that the block belongs to
	 * @param from The 'from' location of the previous jump
	 */
	public PkJump(PkPlayer ply, Location from) {
		man = ply.man;
		this.ply = ply;
		this.main = man.main;
		
		World w = from.getWorld();
		int x = from.getBlockX();
		int y = from.getBlockY();
		int z = from.getBlockZ();
		
		List<Location> bks = new ArrayList<>();
		
		int max = 2;
		int min = 2;
		int maxy = 1;
		
		Difficulty d = ply.getArea().getDifficulty();
		
		if(d.equals(Difficulty.BALANCED)) {
			d = Difficulty.EASY;
			if(ply.getScore() >= 10) {
				d = Difficulty.MEDIUM;
			}
			if(ply.getScore() >= 30) {
				d = Difficulty.HARD;
			}
			if(ply.getScore() >= 70) {
				d = Difficulty.EXPERT;
			}
		}
		
		switch(d) {
			case EASY:
				max = 2;
				break;
			case MEDIUM:
				max = 3;
				break;
			case HARD:
				max = 4;
				min = 3;
				break;
			case EXPERT:
				max = 5;
				min = 4;
				break;
			case BALANCED:
				max = 8;
				break;
			default:
				break;
		}
		
		int r = random(min, max);
		
		//ply.getPlayer().sendMessage(ply.getScore()+":" + d.toString()+" ("+r+")");
		
		if(r > 4) {
			maxy = 0;
		}
		if(r >= 5) {
			r = 5;
			maxy = 0;
		}
		
		if(ply.getJumps().size() >= 2) {
			int prevy = ply.getJumps().get(ply.jumps.size()-1).getFrom().getBlockY();
			int prev2y = ply.getJumps().get(ply.jumps.size()-2).getFrom().getBlockY();
			//ply.ply.sendMessage(prevy+" - "+prev2y+" = "+(prevy - prev2y));
			if(prevy - prev2y > 0) {
				maxy = 0;
			}
		}
		
		
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
		
		HashMap<Object, Double> sc = new HashMap<>();
		for(Location bk : bks) {
			sc.put(bk, (double)getBlockScore(bk, from, ply.getPlayer().getLocation().getYaw()));
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
		
		LinkedHashMap<Object, Double> scs = new LinkedHashMap<>();
		scs = main.sortByValueWithObjectKey(sc, false);
		
		
		
		Object[] scsk = scs.keySet().toArray();
		
		Double last = scs.get(scsk[scsk.length-1]);
		Object selected;
		
		Map<Object, Double> poss = new HashMap<>();
		for(Object key : scs.keySet()) {
			Double v = scs.get(key);
			if(Math.abs(v - last) < 0.0001) {
				poss.put(key, v);
			}
		}
		
		List<Object> posskeys = new ArrayList<>();
		posskeys.addAll(poss.keySet());
		
		//System.out.println(posskeys.size()+" o: " + poss.keySet().size());
		
		int ki = Main.random(0, posskeys.size()-1);
		Object k = posskeys.get(ki);
		selected = k;
		
		
		blocks = new ArrayList<Location>();
		blocks.add((Location) selected);
	}
	
	
	/**
	 * Calculates the score for a particular block location
	 * @param block a {@link org.bukkit.Location Location} of the block to get the score of
	 * @param from a {@link org.bukkit.Location Location} of the 'from' position of the previous jump
	 * @param yaw a float with the player's yaw (left-right looking)
	 * @return the score of a block. (usually 0-10)
	 */
	public int getBlockScore(Location block, Location from, float yaw) {
		if(yaw < 0) {
			yaw +=360;
		}
		
		if(yaw > 180) {
			yaw -= 360;
			yaw = Math.abs(yaw)*-1;
		}
		yaw *= -1; // I did the things below backwards, so this is a quick fix.
		
		
		
		World world = block.getWorld();
		int x = block.getBlockX();
		int y = block.getBlockY();
		int z = block.getBlockZ();
		if(
				!block.getBlock().getType().equals(Material.AIR) 
				|| 
				!new Location(world, x, y-1, z).getBlock().getType().equals(Material.AIR)
				||
				!new Location(world, x, y-2, z).getBlock().getType().equals(Material.AIR)
				||
				!new Location(world, x, y+3, z).getBlock().getType().equals(Material.AIR)
				||
				!new Location(world, x, y+1, z).getBlock().getType().equals(Material.AIR)
				||
				!new Location(world, x, y+2, z).getBlock().getType().equals(Material.AIR)
				||
				!new Location(world, x, y+3, z).getBlock().getType().equals(Material.AIR)
				) {
			return -30;
		}
		PkArea area = ply.getArea();
		
		int score = 10;
		
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
		float closest = 0;
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
		
		if(pos(zc) && zero(xc)) {
			if(floatEquals(closest, 0f)) {
				score += 1;
			} else if(sfloatEquals(closest, 45f) || sfloatEquals(closest, -45f)) {
				score -= 1;
			} else if(sfloatEquals(closest, 90f) || sfloatEquals(closest, -90f)) {
				score -= 3;
			} else {
				score -= 7;
			}
			
		} else if(pos(zc) && neg(xc)) {
			if(floatEquals(closest, 45f)) {
				score += 1;
			} else if(sfloatEquals(closest, 90f) || sfloatEquals(closest, 0f)) {
				score -= 1;
			} else if(sfloatEquals(closest, 135f) || sfloatEquals(closest, -45f)) {
				score -= 3;
			} else {
				score -= 7;
			}
		} else if(neg(xc) && zero(zc)) {
			if(floatEquals(closest, 90f)) {
				score += 1;
			} else if(sfloatEquals(closest, 45f) || sfloatEquals(closest, 135f)) {
				score -= 1;
			} else if(sfloatEquals(closest, 0f) || sfloatEquals(closest, 180f)) {
				score -= 3;
			} else {
				score -= 7;
			}
		} else if(neg(xc) && neg(zc)) {
			if(floatEquals(closest, 135f)) {
				score += 1;
			} else if(sfloatEquals(closest, 90f) || sfloatEquals(closest, 180f)) {
				score -= 1;
			} else if(sfloatEquals(closest, -135f) || sfloatEquals(closest, 45f)) {
				score -= 3;
			} else {
				score -= 7;
			}
		} else if(neg(zc) && zero(xc)) {
			if(floatEquals(closest, 180f)) {
				score += 1;
			} else if(sfloatEquals(closest, 135f) || sfloatEquals(closest, -135f)) {
				score -= 1;
			} else if(sfloatEquals(closest, -90f) || sfloatEquals(closest, 90f)) {
				score -= 3;
			} else {
				score -= 7;
			}
		} else if(pos(xc) && neg(zc)) {
			if(floatEquals(closest, -135f)) {
				score += 1;
			} else if(sfloatEquals(closest, 180f) || sfloatEquals(closest, -90f)) {
				score -= 1;
			} else if(sfloatEquals(closest, 135f) || sfloatEquals(closest, -45f)) {
				score -= 3;
			} else {
				score -= 7;
			}
		} else if(pos(xc) && zero(zc)) {
			if(floatEquals(closest, -90f)) {
				score += 1;
			} else if(sfloatEquals(closest, -45f) || sfloatEquals(closest, -135f)) {
				score -= 1;
			} else if(sfloatEquals(closest, 0f) || sfloatEquals(closest, 180f)) {
				score -= 3;
			} else {
				score -= 7;
			}
		} else if(pos(xc) && pos(zc)) {
			if(floatEquals(closest, -45f)) {
				score += 1;
			} else if(sfloatEquals(closest, 0f) || sfloatEquals(closest, -90f)) {
				score -= 1;
			} else if(sfloatEquals(closest, -135f) || sfloatEquals(closest, 45f)) {
				score -= 3;
			} else {
				score -= 7;
			}
		} else {
			Bukkit.getLogger().warning("[ajParkour] Could not find direction for jump score!");
		}
		
		List<Double> ds = new ArrayList<>();
		for(PkPlayer p : man.getPlayersInArea(area)) {
			if(p.equals(ply)) continue;
			List<Double> l = new ArrayList<>();
			for(PkJump j : p.jumps) {
				Location t = j.getFrom();
				l.add(block.distance(t));
			}
			Collections.sort(l);
			ds.add(l.get(0));
		}
		if(ds.size() > 0) {
			score -= 7-Math.round(ds.get(0));
		}
		
		
		
		int d = (int) Math.round(area.distanceFromWall(block));
		
		if(!area.contains(block)) {
			d = Math.abs(d) * -1;
		}
		
		if(d <= 0) {
			d -= 10;
		}
		
		if(d < 7) {
			score -= 7-d;
		}
	
		
		return score;
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
				type = main.selector.getBlock(ply.getPlayer());
			}
			MaterialParser.placeBlock(l, type);
			l.getBlock().setMetadata("ajpk-prevtype", new FixedMetadataValue(main, prev));
			if(type.toString().equalsIgnoreCase("SKULL") || type.toString().equalsIgnoreCase("PLAYER_HEAD")) {
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
	
	
	private boolean pos(int x) {
		return x > 0;
	}
	
	private boolean neg(int x) {
		return x < 0;
	}
	private boolean zero(int x) {
		return x == 0;
	}
	private boolean sfloatEquals(float o, float t) {
		float d = Math.abs(o - t);
		if(floatEquals(t, 180f)) {
			if(floatEquals(o, -180f)) {
				return true;
			}
		}
		return d < 0.001;
	}
	private boolean floatEquals(float o, float t) {
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
