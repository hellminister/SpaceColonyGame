package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.gamedata.starsystem.Planet;
import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystem;
import ca.dauqui.spacecolonygame.gamedata.tiledata.Building;
import ca.dauqui.spacecolonygame.gamedata.tiledata.ResourceType;

import java.util.*;
import java.util.stream.Collectors;

public class Colony {
    private String colonyName;
    private String commanderName;
    private String aiName;
    private boolean saved;
    private StarSystem starSystem;
    private Planet planet;
    private Site site;
    private final Map<ResourceType, Integer> resourceInventory;
    private final Map<SatelliteType, Integer> satelliteInventory;
    private final Map<Building, Integer> buildingInventory;


    public Colony(){
        saved = false;
        resourceInventory = new HashMap<>();
        satelliteInventory = new HashMap<>();
        buildingInventory = new HashMap<>();
    }

    public StarSystem getStarSystem() {
        return starSystem;
    }

    public Planet getPlanet() {
        return planet;
    }

    public Site getSite() {
        return site;
    }

    private static <T> void add(Map<T, Integer> map, T key, int amount){
        map.compute(key, (k,v)->{
            if (v == null){
                return amount;
            }
            return v + amount;
        });
    }

    public void addInResourceInventory(ResourceType resourceType, int amount){
        add(resourceInventory, resourceType, amount);
    }

    public void addInSatelliteInventory(SatelliteType satelliteType, int amount){
        add(satelliteInventory, satelliteType, amount);
    }

    public void addInBuildingInventory(Building building, int amount){
        add(buildingInventory, building, amount);
    }

    private static <T> int remove(Map<T, Integer> map, T key, int amount){
        int remaining = map.getOrDefault(key, 0) - amount;
        if (remaining > 0){
            map.put(key, remaining);
            return 0;
        } else {
            map.remove(key);
            return remaining * -1; // number of missing items to be removed
        }
    }

    public int removeFromResourceInventory(ResourceType resourceType, int amount){
        return remove(resourceInventory, resourceType, amount);
    }

    public int removeFromSatelliteInventory(SatelliteType satelliteType, int amount){
        return remove(satelliteInventory, satelliteType, amount);
    }

    public int removeFromBuildingInventory(Building building, int amount){
        return remove(buildingInventory, building, amount);
    }

    public Set<ResourceType> getResourceTypesInInventory(){
        return resourceInventory.entrySet().stream().filter(e -> e.getValue() > 0).map(Map.Entry::getKey).collect(Collectors.toUnmodifiableSet());
    }

    public Set<SatelliteType> getSatelliteTypesInInventory(){
        return satelliteInventory.entrySet().stream().filter(e -> e.getValue() > 0).map(Map.Entry::getKey).collect(Collectors.toUnmodifiableSet());
    }

    public Set<Building> getBuildingsInInventory(){
        return buildingInventory.entrySet().stream().filter(e -> e.getValue() > 0).map(Map.Entry::getKey).collect(Collectors.toUnmodifiableSet());
    }


    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setCommanderName(String commanderName) {
        this.commanderName = commanderName;
    }

    public void setAIName(String aiName) {
        this.aiName = aiName;
    }

    public void setPlanet(Planet planet) {
        this.planet = planet;
    }

    public void setSite(Site site) {
        this.site = site;
    }
    public void setStarSystem(StarSystem starSystem) {
        this.starSystem = starSystem;
    }

    public int getAmountInSatelliteInventory(SatelliteType satelliteType) {
        if (!satelliteInventory.containsKey(satelliteType)){
            return 0;
        }
        return satelliteInventory.get(satelliteType);
    }
}