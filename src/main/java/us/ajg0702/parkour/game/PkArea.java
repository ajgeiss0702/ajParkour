package us.ajg0702.parkour.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;

import us.ajg0702.parkour.utils.ParticleRect;

public class PkArea {
	
	Location pos1;
	Location pos2;
	
	Location fallpos;
	
	String name;
	
	int x1;
	int y1;
	int z1;
	int x2;
	int y2;
	int z2;
	
	int highestX;
	int lowestX;
	
	int highestY;
	int lowestY;
	
	int highestZ;
	int lowestZ;
	
	int maxPlayers;
	
	Difficulty difficulty;
	
	/**
	 * Gets the name of the PkArea
	 * @return A string with the name of the area
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the position that the plugin will teleport the player to when they fall.
	 * @return The location of the fallpos. Null if there is not one.
	 */
	public Location getFallPos() {
		Location orig = fallpos;
		if(orig == null) return null;
		int x = orig.getBlockX();
		int y = orig.getBlockY();
		int z = orig.getBlockZ();
		float pitch = orig.getPitch();
		float yaw = orig.getYaw();
		return new Location(orig.getWorld(), x+0.5, y+0.5, z+0.5, yaw, pitch);
	}
	
	/**
	 * Gets pos1 of the area (opposite corner of pos2)
	 * @return the {@link org.bukkit.Location Location} of pos1
	 */
	public Location getPos1() {
		return pos1;
	}
	/**
	 * Gets pos2 of the area (opposite corner of pos1)
	 * @return the {@link org.bukkit.Location Location} of pos2
	 */
	public Location getPos2() {
		return pos2;
	}
	/**
	 * Gets the difficulty of the area.
	 * @return The {@link us.ajg0702.parkour.game.Difficulty Difficulty} of the area.
	 */
	public Difficulty getDifficulty() {
		return difficulty;
	}
	
	/**
	 * Gets the max number of players allowed in this area
	 * @return The max number of players allowed in the area (as an int)
	 */
	public int getMax() {
		return maxPlayers < 0 ? Integer.MAX_VALUE : maxPlayers;
	}

	/**
	 * Initializes an area.
	 * @param name A string with the name of the area
	 * @param p1 The {@link org.bukkit.Location Location} of pos1
	 * @param p2 The {@link org.bukkit.Location Location} of pos2
	 * @param fp The {@link org.bukkit.Location Location} of the fall posotion (can be null)
	 * @param diff The {@link us.ajg0702.parkour.game.Difficulty Difficulty} of the area
	 * @param maxPlayers The max number of players allowed on the area
	 */
	public PkArea(String name, Location p1, Location p2, Location fp, Difficulty diff, int maxPlayers) {
		pos1 = p1;
		pos2 = p2;
		fallpos = fp;
		this.name = name;
		
		this.maxPlayers = maxPlayers;
		
		this.difficulty = diff;
		
		x1 = pos1.getBlockX();
		y1 = pos1.getBlockY();
		z1 = pos1.getBlockZ();
		x2 = pos2.getBlockX();
		y2 = pos2.getBlockY();
		z2 = pos2.getBlockZ();
		
		highestX = Math.max(x1, x2);
		lowestX = Math.min(x1, x2);
		highestY = Math.max(y1, y2);
		lowestY = Math.min(y1, y2);
		highestZ = Math.max(z1, z2);
		lowestZ = Math.min(z1, z2);
	}
	
	/**
	 * Returns a random posotion inside the area
	 * @return A random {@link org.bukkit.Location Location} inside the area
	 */
	public Location getRandomPosition() {
		int x = random(lowestX, highestX);
		int y = random(lowestY, highestY);
		int z = random(lowestZ, highestZ);
		
		float pitch = random(-180f, 180f);
		
		
		
		return new Location(pos1.getWorld(), x, y, z, pitch, 0f);
	}
	
	/**
	 * Returns the distance from the given location to the closest wall
	 * @param l The {@link org.bukkit.Location Location} to check.
	 * @return A Double with the distance from the given location to the closest wall.
	 */
	public Double distanceFromWall(Location l) {
		int x = l.getBlockX();
		int y = l.getBlockY();
		int z = l.getBlockZ();
		
		World w = l.getWorld();
		
		Location px = new Location(w, highestX, y, z);
		Location nx = new Location(w, lowestX, y, z);
		Location py = new Location(w, x, highestY, z);
		Location ny = new Location(w, x, lowestY, z);
		Location pz = new Location(w, x, y, highestZ);
		Location nz = new Location(w, x, y, lowestZ);
		
		List<Double> ls = new ArrayList<>();
		ls.add(l.distance(px));
		ls.add(l.distance(nx));
		ls.add(l.distance(py));
		ls.add(l.distance(ny));
		ls.add(l.distance(pz));
		ls.add(l.distance(nz));
		Collections.sort(ls);
		
		return ls.get(0);
	}
	
	/**
	 * Check if the area contains a location
	 * @param l The {@link org.bukkit.Location Location} to check
	 * @return A boolean indicating if the {@link org.bukkit.Location Location} is in the area.
	 */
	public boolean contains(Location l) {
		int x = l.getBlockX();
		int y = l.getBlockY();
		int z = l.getBlockZ();
		
		
		boolean xg = (x >= lowestX) && (x <= highestX);
		boolean yg = (y >= lowestY) && (y <= highestY);
		boolean zg = (z >= lowestZ) && (z <= highestZ);
		
		return xg && yg && zg;
	}
	
	public void draw() {
		int width = highestZ - lowestZ;
		int length = highestX - lowestX;
		int height = highestY - lowestY;
		
		new ParticleRect(new Location(pos1.getWorld(), lowestX, lowestY, lowestZ), width, length, height).draw();
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
	private static float random(float min, float max) {

		if (min > max) {
			throw new IllegalArgumentException("max must be greater than min: "+min+"-"+max);
		} else if(min == max) {
			return min;
		}

		return (float) (min + Math.random() * (max - min));
	}

}
