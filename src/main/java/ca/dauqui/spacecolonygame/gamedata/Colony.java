package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.gamedata.starsystem.Planet;
import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystem;
import ca.dauqui.spacecolonygame.gamedata.tiledata.Building;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Central transaction ledger and state database entity representing the player colony.
 * Coordinates inventory storage registries and tracks critical campaign metadata parameters
 * including celestial bodies anchoring vectors, player naming, and operational save game flags.
 *
 * @author Jean-François Quintal
 */
public class Colony {


    private final StringProperty colonyName;
    private final StringProperty commanderName;
    private final StringProperty aiName;
    private boolean saved;
    private StarSystem starSystem;
    private Planet planet;
    private Site site;
    private final Map<ResourceType, Integer> resourceInventory;
    private final Map<SatelliteType, Integer> satelliteInventory;
    private final Map<Building, Integer> buildingInventory;

    /**
     * Initializes a fresh colony data snapshot, setting backup flags to false
     * and mapping distinct memory sectors to handle separate item asset classification registers.
     */
    public Colony(){
        saved = false;
        resourceInventory = new HashMap<>();
        satelliteInventory = new HashMap<>();
        buildingInventory = new HashMap<>();

        colonyName = new SimpleStringProperty();
        commanderName = new SimpleStringProperty();
        aiName = new SimpleStringProperty();
    }

    /**
     * @return Underlying operational star system simulation profile mapping orbital data layers
     */
    public StarSystem getStarSystem() {
        return starSystem;
    }

    /**
     * @return Underlying planetary specification node tracking global environmental constraints
     */
    public Planet getPlanet() {
        return planet;
    }

    /**
     * @return Current active multi-tier geographic grid map site selected for landing operations
     */
    public Site getSite() {
        return site;
    }

    /**
     * Internal generic mutation utility appending item quantities safely onto mapped data registries.
     * Handles key initialization automatically if target signatures do not yet exist inside memory.
     *
     * @param <T>    The structural asset model key type managed by the register map
     * @param map    The physical memory storage tracking map undergoing mutation
     * @param key    The precise data signature identifier matching the incoming item profile
     * @param amount Total integer volumetric delta metrics to push onto current asset indexes
     */
    private static <T> void add(Map<T, Integer> map, T key, int amount){
        map.compute(key, (_, v)->{
            if (v == null){
                return amount;
            }
            return v + amount;
        });
    }

    /**
     * Increases stocked reserves for a specific raw cargo raw material specification.
     *
     * @param resourceType The target natural raw component classification entry
     * @param amount       Total metrics units count to load into inventory bays
     */
    public void addInResourceInventory(ResourceType resourceType, int amount){
        add(resourceInventory, resourceType, amount);
    }

    /**
     * Increases stocked reserves for a specific orbital hardware telemetry asset.
     *
     * @param satelliteType The target scan probe array model signature blueprint
     * @param amount        Total hardware component units count to register into storage bays
     */
    public void addInSatelliteInventory(SatelliteType satelliteType, int amount){
        add(satelliteInventory, satelliteType, amount);
    }

    /**
     * Increases stocked reserves for a specific prefabricated architectural deployment module.
     *
     * @param building The target layout infrastructure blueprint asset
     * @param amount   Total prefabricated structural components count to push into inventory bays
     */
    public void addInBuildingInventory(Building building, int amount){
        add(buildingInventory, building, amount);
    }

    /**
     * Internal generic mutation utility processing asset consumption patterns, returning
     * the total deficit slice if requested quantities exceed current available stock balances.
     *
     * @param <T>    The structural asset classification type identifier signature
     * @param map    The targeted data inventory map tracking current allocation balances
     * @param key    The specific item registration key to subtract from records
     * @param amount The requested allocation transaction volume parameter
     * @return Total volume of items missing from stock to fully clear the transaction request (0 if successful)
     */
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

    /**
     * Deducts a specific volume of raw materials from cargo inventory registers.
     *
     * @param resourceType The specific material entry token targeted for extraction
     * @param amount       The transaction request withdrawal quantity parameter
     * @return Deficit index tracking missing item balances required to complete the draw loop
     */
    public int removeFromResourceInventory(ResourceType resourceType, int amount){
        return remove(resourceInventory, resourceType, amount);
    }

    /**
     * Deducts a specific volume of orbital payload items from satellite registers.
     *
     * @param satelliteType The specific telemetry item model targeted for consumption
     * @param amount        The transaction request withdrawal quantity parameter
     * @return Deficit index tracking missing item balances required to complete the draw loop
     */
    public int removeFromSatelliteInventory(SatelliteType satelliteType, int amount){
        return remove(satelliteInventory, satelliteType, amount);
    }

    /**
     * Deducts a specific volume of prefabricated building modules from structural registers.
     *
     * @param building The specific structural deployment blueprint targeted for deduction
     * @param amount   The transaction request withdrawal quantity parameter
     * @return Deficit index tracking missing item balances required to complete the draw loop
     */
    public int removeFromBuildingInventory(Building building, int amount){
        return remove(buildingInventory, building, amount);
    }

    /**
     * @return An unmodifiable view set containing all distinct resource keys whose balances sit above zero
     */
    public Set<ResourceType> getResourceTypesInInventory(){
        return resourceInventory.entrySet().stream().filter(e -> e.getValue() > 0).map(Map.Entry::getKey).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * @return An unmodifiable view set containing all distinct satellite keys whose balances sit above zero
     */
    public Set<SatelliteType> getSatelliteTypesInInventory(){
        return satelliteInventory.entrySet().stream().filter(e -> e.getValue() > 0).map(Map.Entry::getKey).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * @return An unmodifiable view set containing all distinct building keys whose balances sit above zero
     */
    public Set<Building> getBuildingsInInventory(){
        return buildingInventory.entrySet().stream().filter(e -> e.getValue() > 0).map(Map.Entry::getKey).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * @param saved True to toggle persistence confirmations marking this campaign file as safely written to storage
     */
    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    /**
     * @return True if all dynamic data modifications are cleanly serialized on physical disk records
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * @param commanderName The alphanumeric text string to assign onto the master player profile
     */
    public void setCommanderName(String commanderName) {
        this.commanderName.set(commanderName);
    }

    /**
     * @param aiName The alphanumeric text string to assign onto the localized digital companion proxy
     */
    public void setAIName(String aiName) {
        this.aiName.set(aiName);
    }

    /**
     * @return The reactive string property wrapper tracking the player commander profile adjustments
     */
    public ReadOnlyStringProperty commanderNameProperty() {
        return commanderName;
    }

    /**
     * @return The reactive string property wrapper tracking the digital companion proxy adjustments
     */
    public ReadOnlyStringProperty aiNameProperty() {
        return aiName;
    }

    /**
     * @return The reactive string property wrapper tracking the colony name adjustments
     */
    public ReadOnlyStringProperty colonyNameProperty() {
        return colonyName;
    }

    /**
     * @param colonyName The new name string to assign onto the colony property registry
     */
    public void setColonyName(String colonyName) {
        this.colonyName.set(colonyName);
    }

    /**
     * @param planet The target planetary core object profile selected for launch destination coordinates
     */
    public void setPlanet(Planet planet) {
        this.planet = planet;
    }

    /**
     * @param site The target geographic multi-tier matrix grid zone selected for landing pad setups
     */
    public void setSite(Site site) {
        this.site = site;
    }

    /**
     * @param starSystem The target celestial simulation system layout focused by orbital flight calculations
     */
    public void setStarSystem(StarSystem starSystem) {
        this.starSystem = starSystem;
    }

    /**
     * Fetches current available hardware allocation balances registered for an explicit satellite asset.
     *
     * @param satelliteType The target scan probe model blueprint to evaluate
     * @return Total volume of hardware items currently held in warehouse stock repositories
     */
    public int getAmountInSatelliteInventory(SatelliteType satelliteType) {
        return satelliteInventory.getOrDefault(satelliteType, 0);
    }
}