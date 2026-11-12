package ca.dauqui.spacecolonygame.util;

import ca.dauqui.spacecolonygame.settings.GameSettings;

import java.util.Random;

/**
 * Deterministic {@link Random} implementation automatically synchronized with {@link GameSettings.LongSetting#WORLD_SEED}.
 * Reseeds itself immediately whenever the active world seed changes in user settings.
 *
 * @author Jean-François Quintal
 */
public final class GameRandom extends Random {

    /**
     * Global singleton pseudo-random number generator shared across procedural generation engines.
     */
    public static final GameRandom RANDOM = new GameRandom();

    static {
        GameSettings.LongSetting.WORLD_SEED.readOnlyProperty().addListener((_, _, newSeed) -> {
            if (newSeed != null) {
                RANDOM.applySeed(newSeed.longValue());
            }
        });
    }

    /**
     * Private singleton constructor initializing the generator with the currently active world seed.
     */
    private GameRandom() {
        super(GameSettings.LongSetting.WORLD_SEED.get());
    }

    /**
     * Bypasses the public mutation guard to apply a new seed internally from {@link GameSettings}.
     *
     * @param seed The new 64-bit seed value to initialize the underlying linear congruential generator
     */
    private void applySeed(long seed) {
        super.setSeed(seed);
    }

    /**
     * Blocks direct external reseeding to enforce centralized state management through {@link GameSettings}.
     * Permits only the initial seed assignment invoked by the superclass constructor during bootstrap.
     *
     * @param seed The 64-bit seed value
     * @throws UnsupportedOperationException If invoked externally after singleton initialization
     */
    @Override
    public synchronized void setSeed(long seed) {
        if (RANDOM != null) {
            throw new UnsupportedOperationException("Modify GameSettings.LongSetting.WORLD_SEED instead of calling setSeed directly.");
        }
        super.setSeed(seed);
    }
}