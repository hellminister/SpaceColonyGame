package ca.dauqui.spacecolonygame.gui.newgame.cargoloader;

import ca.dauqui.spacecolonygame.gamedata.ResourceType;
import ca.dauqui.spacecolonygame.gamedata.SatelliteType;
import ca.dauqui.spacecolonygame.gamedata.tiledata.Building;
import ca.dauqui.spacecolonygame.util.ModdablePropertiesLoader;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Static computational provider parsing operational cargo configurations from disk properties.
 * Assembles standalone immutable inventory data entries representing the initial trade market setup.
 * All operations run deterministically on structural data registries.
 *
 * @author Jean-François Quintal
 */
final class CargoMarket {

    private static final String WEIGHT_SUFFIX = ".weight";
    private static final String COST_SUFFIX = ".cost";
    private static final String STARTING_COUNT_SUFFIX = ".startingCount";
    private static final String QUANTITY_PER_UNIT_SUFFIX = ".quantityPerUnit";

    private CargoMarket() {
        /* This utility class should not be instantiated */
    }

    private static final String MARKET_PATH = "/ca/dauqui/spacecolonygame/data/cargoMarket.properties";
    private static final Properties marketProps = ModdablePropertiesLoader.load(MARKET_PATH);

    private static List<ResourceItem> resourceItems = null;
    private static List<SatelliteItem> satelliteItems = null;
    private static List<BuildingItem> buildingItems = null;


    /**
     * Instantiates transactional UI records for raw materials filtered via the global resource data layout.
     *
     * @return An unmodifiable snapshot collection list containing fully configured ResourceItems
     */
    public static List<ResourceItem> createResourceMarketItems() {
        if (resourceItems == null) {
            var items = new ArrayList<ResourceItem>();
            Set<String> uniqueIds = extractIdsForPrefix("RESOURCE.");

            for (String id : uniqueIds) {
                // Instant data validation retrieval utilizing core structural constants loaders
                ResourceType resourceData = ResourceType.get(id);
                if (resourceData != null) {
                    String baseKey = "RESOURCE." + id;

                    int weight = Integer.parseInt(marketProps.getProperty(baseKey + WEIGHT_SUFFIX, "0"));
                    int cost = Integer.parseInt(marketProps.getProperty(baseKey + COST_SUFFIX, "0"));
                    int qtyPerUnit = Integer.parseInt(marketProps.getProperty(baseKey + QUANTITY_PER_UNIT_SUFFIX, "1"));
                    int startingCount = Integer.parseInt(marketProps.getProperty(baseKey + STARTING_COUNT_SUFFIX, "0"));

                    items.add(new ResourceItem(resourceData, weight, cost, startingCount, qtyPerUnit));
                }
            }
            resourceItems = Collections.unmodifiableList(items);
        }
        return resourceItems;
    }

    /**
     * Instantiates transaction UI entries for satellites and probes.
     *
     * @return An unmodifiable snapshot collection list containing fully configured SatelliteItems
     */
    public static List<SatelliteItem> createSatelliteMarketItems() {
        if (satelliteItems == null) {
            var items = new ArrayList<SatelliteItem>();
            Set<String> uniqueIds = extractIdsForPrefix("SATELLITE.");

            for (String id : uniqueIds) {
                SatelliteType satelliteData = SatelliteType.get(id);
                if (satelliteData != null) {
                    String baseKey = "SATELLITE." + id;

                    int weight = Integer.parseInt(marketProps.getProperty(baseKey + WEIGHT_SUFFIX, "0"));
                    int cost = Integer.parseInt(marketProps.getProperty(baseKey + COST_SUFFIX, "0"));
                    int startingCount = Integer.parseInt(marketProps.getProperty(baseKey + STARTING_COUNT_SUFFIX, "0"));

                    items.add(new SatelliteItem(satelliteData, weight, cost, startingCount));
                }
            }
            satelliteItems = Collections.unmodifiableList(items);
        }
        return satelliteItems;
    }

    /**
     * Instantiates transaction UI entries for buildings.
     *
     * @return An unmodifiable snapshot collection list containing fully configured SatelliteItems
     */
    public static List<BuildingItem> createBuildingMarketItems() {
        if (buildingItems == null) {
            var items = new ArrayList<BuildingItem>();
            Set<String> uniqueIds = extractIdsForPrefix("BUILDING.");
            for (String id : uniqueIds) {

                Building temporary = null;
                if (temporary != null) {
                    String baseKey = "BUILDING." + id;
                    int weight = Integer.parseInt(marketProps.getProperty(baseKey + WEIGHT_SUFFIX, "0"));
                    int cost = Integer.parseInt(marketProps.getProperty(baseKey + COST_SUFFIX, "0"));
                    int startingCount = Integer.parseInt(marketProps.getProperty(baseKey + STARTING_COUNT_SUFFIX, "0"));

                    items.add(new BuildingItem(weight, cost, startingCount, temporary));
                }
            }
            buildingItems = Collections.unmodifiableList(items);
        }
        return buildingItems;
    }

    /**
     * @return The starting ledger balance credits allowed for early orbital manifest purchasing
     */
    public static int getInitialMoney(){
        return Integer.parseInt(marketProps.getProperty("initialMoney", "1000"));
    }

    /**
     * @return The total structural payload mass threshold boundary allowed inside the spacecraft hull
     */
    public static int getMaxWeight(){
        return Integer.parseInt(marketProps.getProperty("maxWeight", "1000"));
    }

    /**
     * Segregates and collects structural tokens from property file keys based on exact data prefixes.
     * E.g., translates "RESOURCE.ORE_A.weight" into an isolated unique identity string key matching "ORE_A".
     *
     * @param prefix The isolated target module text layer to intercept
     * @return A unique set containing capitalized asset classification strings
     */
    private static Set<String> extractIdsForPrefix(String prefix) {
        Set<String> ids = new HashSet<>();
        for (String key : marketProps.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                String remainder = key.substring(prefix.length());
                String id = remainder.substring(0, remainder.indexOf('.'));
                ids.add(id.toUpperCase());
            }
        }
        return ids;
    }
}