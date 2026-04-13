package ca.dauqui.spacecolonygame.gamedata.tiledata;

import java.util.EnumMap;
import java.util.List;

public class Building extends TileData {
    private int unused;
    private final EnumMap<ResourceType, Integer> resourcesToBuild;
    private final EnumMap<ResourceType, Integer> resourcesToOperate;
    private int remainingBuildTime;
    private int remainingLifeTime;
    private List<PopulationSlot> populationToBuild;
    private List<PopulationSlot> populationToOperate;


    public Building() {
        this.resourcesToBuild = new EnumMap<>(ResourceType.class);
        this.resourcesToOperate = new EnumMap<>(ResourceType.class);
    }
}