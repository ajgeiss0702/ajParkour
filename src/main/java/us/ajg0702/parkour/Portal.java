package us.ajg0702.parkour;

import org.bukkit.Location;
import us.ajg0702.parkour.game.PkArea;

public class Portal {
	
	String name;
	
	Location loc;
	PkArea area;
	
	public Portal(String name, Location loc, PkArea a) {
		this.loc = loc;
		this.area = a;
		this.name = name;
	}
	
	
	public Location getLoc() {
		return loc;
	}
	
	
	public PkArea getArea() {
		return area;
	}
	
	public String getName() {
		return name;
	}

}
