package us.ajg0702.parkour.setup;

import us.ajg0702.parkour.ParkourPlugin;
import us.ajg0702.parkour.game.ParkourArea;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SetupManager {

    private final ParkourPlugin plugin;


    private final Map<String, InProgressArea> inProgressAreas = new HashMap<>();


    public SetupManager(ParkourPlugin plugin) {
        this.plugin = plugin;
    }

    public InProgressArea getInProgressArea(String areaName) {
        return inProgressAreas.get(areaName.toLowerCase());
    }

    public InProgressArea createInProgressArea(String areaName) {
        if (inProgressAreas.containsKey(areaName)) {
            throw new IllegalStateException("InProgress area already exists for " + areaName);
        }
        if(plugin.getManager().getAreaNames().contains(areaName)) {
            throw new IllegalStateException("Area already exists for " + areaName);
        }

        InProgressArea area = new InProgressArea(areaName);
        inProgressAreas.put(areaName, area);
        return area;
    }

    public InProgressArea getOrImport(String areaName) {
        if (inProgressAreas.containsKey(areaName)) {
            return inProgressAreas.get(areaName);
        } else {
            ParkourArea area = plugin.getManager().getArea(areaName);
            if(area == null) return null;
            InProgressArea inProgressArea = new InProgressArea(
                    areaName,
                    area.getBox().getPosition1().getLocation(),
                    area.getBox().getPosition2().getLocation(),
                    area.getFallPosition(),
                    area.getDifficulty().getName()
            );
            inProgressAreas.put(area.getName(), inProgressArea);
            return inProgressArea;
        }
    }

    public Set<String> getNames() {
        return inProgressAreas.keySet();
    }



}
