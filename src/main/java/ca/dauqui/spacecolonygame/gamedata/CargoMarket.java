package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.gamedata.tiledata.ResourceType;
import ca.dauqui.spacecolonygame.gui.newgame.ResourceItem;
import ca.dauqui.spacecolonygame.gui.newgame.SatelliteItem;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public final class CargoMarket {
    private CargoMarket() {
        /* This utility class should not be instantiated */
    }

    private static final Logger LOG = Logger.getLogger(CargoMarket.class.getName());
    private static final String MARKET_PATH = "/ca/dauqui/spacecolonygame/data/cargoMarket.properties";
    private static final Properties marketProps = new Properties();

    static {
        try (InputStream is = CargoMarket.class.getResourceAsStream(MARKET_PATH)) {
            if (is == null) throw new java.io.FileNotFoundException("Market file not found : " + MARKET_PATH);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                marketProps.load(br);
            }
        } catch (Exception e) {
            LOG.severe(() -> "Error loading CargoMarket finance data : " + e.getMessage());
        }
    }

    /**
     * Crée les items d'UI pour les ressources de manière ciblée via le préfixe RESOURCE.
     */
    public static List<ResourceItem> createResourceMarketItems() {
        List<ResourceItem> items = new ArrayList<>();
        Set<String> uniqueIds = extractIdsForPrefix("RESOURCE.");

        for (String id : uniqueIds) {
            String baseKey = "RESOURCE." + id;
            int weight = Integer.parseInt(marketProps.getProperty(baseKey + ".weight", "0"));
            int cost = Integer.parseInt(marketProps.getProperty(baseKey + ".cost", "0"));
            int qtyPerUnit = Integer.parseInt(marketProps.getProperty(baseKey + ".quantityPerUnit", "1"));
            int startingCount = Integer.parseInt(marketProps.getProperty(baseKey + ".startingCount", "0"));


            // Récupération instantanée du type brut via ton registre DataRegistry/TypesafeConstants
            ResourceType resourceData = ResourceType.get(id);
            items.add(new ResourceItem(resourceData, weight, cost, startingCount, qtyPerUnit));
        }
        return Collections.unmodifiableList(items);
    }

    /**
     * Crée les items d'UI pour les satellites de manière ciblée via le préfixe SATELLITE.
     */
    public static List<SatelliteItem> createSatelliteMarketItems() {
        List<SatelliteItem> items = new ArrayList<>();
        Set<String> uniqueIds = extractIdsForPrefix("SATELLITE.");

        for (String id : uniqueIds) {
            String baseKey = "SATELLITE." + id;
            int weight = Integer.parseInt(marketProps.getProperty(baseKey + ".weight", "0"));
            int cost = Integer.parseInt(marketProps.getProperty(baseKey + ".cost", "0"));
            int startingCount = Integer.parseInt(marketProps.getProperty(baseKey + ".startingCount", "0"));


            SatelliteType satelliteData = SatelliteType.get(id);
            items.add(new SatelliteItem(satelliteData, weight, cost, startingCount));
        }
        return Collections.unmodifiableList(items);
    }

    public static int getInitialMoney(){
        return Integer.parseInt(marketProps.getProperty("initialMoney", "1000"));
    }

    public static int getMaxWeight(){
        return Integer.parseInt(marketProps.getProperty("maxWeight", "1000"));
    }

    /**
     * Utilitaire pour extraire de façon unique les IDs d'une catégorie donnée (ex: RESOURCE)
     */
    private static Set<String> extractIdsForPrefix(String prefix) {
        Set<String> ids = new HashSet<>();
        for (String key : marketProps.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                // Découpe la clé (ex: "RESOURCE.ORE_A.weight" -> "ORE_A")
                String remainder = key.substring(prefix.length());
                String id = remainder.substring(0, remainder.indexOf('.'));
                ids.add(id.toUpperCase());
            }
        }
        return ids;
    }
}