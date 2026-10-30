package ca.dauqui.spacecolonygame.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class SoftMapCache<K, V> {

    private final ConcurrentHashMap<K, KeyedSoftReference<K, V>> cache = new ConcurrentHashMap<>();
    private final ReferenceQueue<V> referenceQueue = new ReferenceQueue<>();

    public void put(K key, V value) {
        if (key == null || value == null) {
            return;
        }

        cleanCache();
        cache.put(key, new KeyedSoftReference<>(key, value, referenceQueue));
    }

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

    public V computeIfAbsent(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
        if (key == null || mappingFunction == null) return null;

        cleanCache();

        while (true) {
            // 1. Lecture rapide hors verrou (Optimiste)
            KeyedSoftReference<K, V> ref = cache.get(key);
            if (ref != null) {
                V value = ref.get();
                if (value != null) {
                    return value; // Trouvé et vivant !
                }
                // Référence morte détectée hors verrou, on tente de la supprimer proprement
                cache.remove(key, ref);
            }

            // 2. Si non trouvé ou supprimé, on calcule la valeur à l'extérieur du computeIfAbsent
            // Cela évite de bloquer la Map pendant les I/O (ex: chargement d'image)
            V newValue = mappingFunction.apply(key);
            if (newValue == null) {
                return null;
            }

            // 3. On insère de manière atomique en utilisant putIfAbsent
            KeyedSoftReference<K, V> newRef = new KeyedSoftReference<>(key, newValue, referenceQueue);
            KeyedSoftReference<K, V> existingRef = cache.putIfAbsent(key, newRef);

            if (existingRef == null) {
                return newValue; // Insertion réussie !
            }

            // Si un autre thread a inséré une référence entre temps, on vérifie si elle est vivante
            V existingValue = existingRef.get();
            if (existingValue != null) {
                return existingValue; // L'autre thread a gagné, on utilise sa valeur
            }

            // Si la référence insérée par l'autre thread était déjà morte, la boucle while(true) réessayera au prochain tour
            cache.remove(key, existingRef);
        }
    }

    private void cleanCache() {
        Reference<? extends V> ref;
        // Le poll() est non-bloquant, ce qui est parfait ici
        while ((ref = referenceQueue.poll()) != null) {
            if (ref instanceof KeyedSoftReference<?, ?> keyedRef) { // Utilisation du Pattern Matching (Java 14+)
                @SuppressWarnings("unchecked")
                KeyedSoftReference<K, V> safeRef = (KeyedSoftReference<K, V>) keyedRef;
                K key = safeRef.getKey();
                if (key != null) {
                    cache.remove(key, safeRef);
                }
            }
        }
    }

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