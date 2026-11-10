package ca.dauqui.spacecolonygame.util;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract class used to load static information about the type of data using this structure
 * Acts like a sort of weak enum
 * @param <T> The type of the class using this structure
 */
public abstract class TypesafeConstants<T extends EnumLike> {
    private final AtomicInteger indexForNextValue = new AtomicInteger(0);
    /**
     * The immutable map containing each available objects linked to its key
     */
    private final Map<String, T> registryMap;

    /**
     * @param configPath the file path of the file containing the data
     */
    protected TypesafeConstants(String configPath) {
        Map<String, T> tempMap = new HashMap<>();
        loadProperties(configPath, tempMap);
        this.registryMap = Collections.unmodifiableMap(tempMap);
    }

    /**
     * Loads the data from the files and generates the data structures for each object
     * @param configPath the path to the description of the objects
     * @param targetMap The map containing the objects
     */
    private void loadProperties(String configPath, Map<String, T> targetMap) {
        Properties props = ModdablePropertiesLoader.load(configPath);

        parseProperties(props, targetMap);

        fallback(targetMap);
    }

    /**
     * Generates and returns a unique, sequential zero-based index mapping to mimic
     * native enum ordinals for file-driven configuration constants.
     * Utilizes atomic, thread-safe counter operations to safely guarantee
     * unique allocation barriers across asynchronous bootstrap loading pipelines.
     *
     * @return The next unique, sequential zero-based ordinal index scalar
     */
    protected int nextOrdinal(){
        return indexForNextValue.getAndIncrement();
    }

    /**
     * takes the data from the file and creates each object and stores them in the map
     * @param props The data about all the objects
     * @param targetMap the map that will contain all the objects
     */
    protected abstract void parseProperties(Properties props, Map<String, T> targetMap);

    /**
     * implement this if you need to create an object that must be present in the map
     * @param targetMap The map containing the objects
     */
    protected void fallback(Map<String, T> targetMap){
        //To be implemented when needed
    }

    /**
     * Returns the asked object
     * @param id the id of the wanted object
     * @return the object corresponding to the id (or null if the id doesn't exist)
     */
    public T get(String id) {
        return registryMap.get(id);
    }

    /**
     * Returns all the objects of this type
     * @return an immutable collection containing all the objects of this type
     */
    public Collection<T> getAll() {
        return registryMap.values();
    }

    /**
     * return the map containing the objects
     * @return an immutable map containing all the object and key of this type
     */
    public Map<String, T> getInternalMap() {
        return registryMap;
    }
}