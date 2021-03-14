package us.ajg0702.parkour.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;

/**
* Created by Connor on 12/3/2016.
*/
public class ParticleRect {

    ArrayList<ParticlePoint> sides = new ArrayList<>();
    Location start;

    public ParticleRect(Location start, double width, double length,double height){
        this.start = start;
        Vector A = new Vector(0,0,0);
        Vector B = new Vector(length,0,0);
        Vector C = new Vector(0,0,width);
        Vector D = new Vector(0,height,0);
        Vector E = new Vector(0,height,width);
        Vector F = new Vector(length,height,0);
        Vector G = new Vector(length,0,width);
        sides.add(new ParticlePoint(A, B));
        sides.add(new ParticlePoint(A, C));
        sides.add(new ParticlePoint(A, D));
        sides.add(new ParticlePoint(C, D));
        sides.add(new ParticlePoint(B, D));
        sides.add(new ParticlePoint(C, B));
        sides.add(new ParticlePoint(B, C));
        sides.add(new ParticlePoint(D, C));
        sides.add(new ParticlePoint(D, B));
        sides.add(new ParticlePoint(E, B));
        sides.add(new ParticlePoint(F, C));
        sides.add(new ParticlePoint(G, D));
    }

    public Vector getPostion(double blocksAway,Vector origin, Vector direction) {
        return origin.clone().add(direction.clone().normalize().multiply(blocksAway));
    }

    public ArrayList<Vector> traverse(Vector origin, Vector direction) {
        ArrayList<Vector> positions = new ArrayList<>();
        for (double d = 0; d <= direction.length(); d += 0.1) {
            positions.add(getPostion(d,origin,direction));
        }
        return positions;
    }

    public void draw(){
            for(ParticlePoint point : sides){
            for(Vector position : traverse(point.origin,point.direction)){
                position = start.toVector().clone().add(position);
                //start.getWorld().playEffect(position.toLocation(start.getWorld()), Effect.COLOURED_DUST, 0);
                start.getWorld().spawnParticle(Particle.FLAME, position.toLocation(start.getWorld()), 1, 0, 0, 0, 0);
            }
        }
    }

    private static class ParticlePoint {
        Vector origin,direction;
        ParticlePoint(Vector origin, Vector direction) {
            this.origin = origin;
            this.direction = direction;
        }
    }

}
