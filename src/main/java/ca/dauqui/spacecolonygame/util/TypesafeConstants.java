package ca.dauqui.spacecolonygame.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public abstract class TypesafeConstants<T> {
    private final Logger log;
    private final Map<String, T> registryMap;

    protected TypesafeConstants(Class<?> childClass, String configPath) {
        this.log = Logger.getLogger(childClass.getName());

        Map<String, T> tempMap = new HashMap<>();
        loadProperties(childClass, configPath, tempMap);
        this.registryMap = Collections.unmodifiableMap(tempMap);
    }

    private void loadProperties(Class<?> childClass, String configPath, Map<String, T> targetMap) {
        Properties props = new Properties();
        try (InputStream is = childClass.getResourceAsStream(configPath)) {
            if (is == null) {
                throw new java.io.FileNotFoundException("Resource file not found: " + configPath);
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                props.load(br);
            }

            parseProperties(props, targetMap);

        } catch (Exception e) {
            log.severe(() -> "Critical error loading registry configuration: " + e.getMessage());
        }

        fallback(targetMap);
    }

    protected abstract void parseProperties(Properties props, Map<String, T> targetMap);
    protected abstract void fallback(Map<String, T> targetMap);

    public T get(String id) {
        T instance = registryMap.get(id.trim().toUpperCase());
        return instance;
    }

    public Collection<T> getAll() {
        return registryMap.values();
    }

    public Map<String, T> getInternalMap() {
        return registryMap;
    }
}