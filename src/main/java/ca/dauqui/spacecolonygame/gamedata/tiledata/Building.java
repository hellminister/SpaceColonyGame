package ca.dauqui.spacecolonygame.gamedata.tiledata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* TODO create and immutable version of this to serve as a blueprint */
public class Building extends TileData {
    private int unused;
    private final Map<ResourceType, Integer> resourcesToBuild;
    private final Map<ResourceType, Integer> resourcesToOperate;
    private int remainingBuildTime;
    private int remainingLifeTime;
    private List<PopulationSlot> populationToBuild;
    private List<PopulationSlot> populationToOperate;


    public Building() {
        this.resourcesToBuild = new HashMap<>();
        this.resourcesToOperate = new HashMap<>();
    }
}