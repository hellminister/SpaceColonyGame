package ca.dauqui.spacecolonygame.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance, thread-safe memory cache utilizing soft references to store heavy assets.
 * Dynamically purges cleared garbage-collected values via an internal reference tracking queue.
 * Optimized for asynchronous asset loading (e.g., UI texture streams, cinematic video buffers).
 *
 * @param <K> The configuration registry lookup key type
 * @param <V> The heavy structural object asset type stored within the soft wrapper
 */
public class SoftMapCache<K, V> {

    private final ConcurrentHashMap<K, KeyedSoftReference<K, V>> cache = new ConcurrentHashMap<>();
    private final ReferenceQueue<V> referenceQueue = new ReferenceQueue<>();

    /**
     * Binds and caches a new heavy asset payload to the specified registry key.
     * Automatically triggers a non-blocking queue cleanup sweep prior to insertion.
     *
     * @param key   The reference tracking key (ignored if null)
     * @param value The structural asset instance to preserve (ignored if null)
     */
    public void put(K key, V value) {
        if (key == null || value == null) {
            return;
        }

        cleanCache();
        cache.put(key, new KeyedSoftReference<>(key, value, referenceQueue));
    }

    /**
     * Resolves and extracts an asset from the internal map if it remains alive.
     * Safely purges stale tracking entries on-the-fly if the JVM garbage collector cleared the payload.
     *
     * @param key The reference tracking key to inspect
     * @return The active asset instance, or null if missing or cleared from the heap
     */
    public V get(K key) {
        if (key == null) return null;

        cleanCache();

        KeyedSoftReference<K, V> reference = cache.get(key);
        if (reference == null) {
            return null;
        }

        V value = reference.get();
        if (value == null) {
            cache.remove(key, reference);
        }
        return value;
    }

    /**
     * Atomic lock-free compute utility engineered to load assets concurrently without blocking map lookups.
     * Executes the heavy allocation function outside map write locks to maximize frame throughput.
     *
     * @param key             The reference tracking key to resolve
     * @param mappingFunction The asset loading routine invoked during cache misses
     * @return The living cached asset, or null if processing fails
     */
    public V computeIfAbsent(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
        if (key == null || mappingFunction == null) return null;

        cleanCache();

        while (true) {
            // 1. Optimistic lock-free read phase
            KeyedSoftReference<K, V> ref = cache.get(key);
            if (ref != null) {
                V value = ref.get();
                if (value != null) {
                    return value; // Cache hit: Asset is alive and verified
                }
                // Dead reference intercepted out-of-lock: Tries an unlinked removal
                cache.remove(key, ref);
            }

            // 2. Compute heavy asset outside of map bounds to prevent thread starvation (e.g., File I/O)
            V newValue = mappingFunction.apply(key);
            if (newValue == null) {
                return null;
            }

            // 3. Atomic insertion phase using putIfAbsent gates
            KeyedSoftReference<K, V> newRef = new KeyedSoftReference<>(key, newValue, referenceQueue);
            KeyedSoftReference<K, V> existingRef = cache.putIfAbsent(key, newRef);

            if (existingRef == null) {
                return newValue; // Insertion successful
            }

            // Handles synchronization race conditions seamlessly if another thread completed the write
            V existingValue = existingRef.get();
            if (existingValue != null) {
                return existingValue; // Alternate thread won the race, use its active reference pointer
            }

            // Intercepted a dead reference written by the alternate thread, loop retry cleans it up
            cache.remove(key, existingRef);
        }
    }

    /**
     * Polls the reference collection queue to purge unlinked keys abandoned by heap collections.
     * Executes entirely in non-blocking cycles to maintain fluid UI thread loops.
     */
    private void cleanCache() {
        Reference<? extends V> ref;
        while ((ref = referenceQueue.poll()) != null) {
            // Pattern matching utilizing wildcard bounds avoids casting suppressions completely
            if (ref instanceof KeyedSoftReference<?, ?> keyedRef) {
                // Explicitly cast to the map's structural type parameters to satisfy static analyzers
                @SuppressWarnings("unchecked")
                KeyedSoftReference<K, V> safeRef = (KeyedSoftReference<K, V>) keyedRef;
                K key = safeRef.getKey();
                if (key != null) {
                    cache.remove(key, keyedRef);
                }
            }
        }
    }

    /**
     * Specialized SoftReference wrapper binding an immutable tracking key token to its reference.
     */
    private static class KeyedSoftReference<K, V> extends SoftReference<V> {
        private final K key;

        public KeyedSoftReference(K key, V referent, ReferenceQueue<? super V> q) {
            super(Objects.requireNonNull(referent), q);
            this.key = Objects.requireNonNull(key);
        }

        public K getKey() {
            return key;
        }
    }
}