package ca.dauqui.spacecolonygame.gamedata.starsystem;

import ca.dauqui.spacecolonygame.util.GameRandom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Architectural data model representing an active operational stellar system.
 * Dynamically uncovers packaged data profiles and instantiates procedural planets
 * using pseudorandom range distributions based on astronomical baseline constraints.
 *
 * @author Jean-François Quintal
 */
public class StarSystem {
    private final StarSystemAsset staticInfo;
    private final List<Planet> planets;
    private static final Random random = GameRandom.RANDOM;

    /**
     * Constructs a live star system simulation node, unpacking core embedded planet presets
     * and procedurally spawning additional hidden worlds within deterministic configuration limits.
     *
     * @param probed The core foundational master blueprint tracking static stellar registry data
     */
    public StarSystem(StarSystemAsset probed) {
        this.staticInfo = probed;
        planets = new ArrayList<>();
        PlanetGenerator planetGenerator = new PlanetGenerator();

        // Layer 1: Instantiate pre-configured baseline celestial bodies registered inside game records
        for (PlanetAsset ps : probed.getKnownPlanets()){
            Planet planet = planetGenerator.generate(probed, ps);
            planets.add(planet);
        }

        // Layer 2: Procedurally roll and inject undiscovered hidden worlds into the orbital lane corridors
        int min = Math.max(probed.minNumberOfPlanets(), 0);
        int max = Math.max(probed.maxNumberOfPlanets(), 2);

        int addPlanets = random.nextInt(min, max+1);

        for (int i = 0; i < addPlanets; i++) {
            Planet planet = planetGenerator.generate(probed);
            planets.add(planet);
        }

        // Sort all registered celestial nodes sequentially to map true layout distances from the sun center
        planets.sort(Comparator.comparingDouble(Planet::distance));

    }

    /**
     * @return The dynamically populated list collection holding all generated independent Planet entries
     */
    public List<Planet> getPlanets() {
        return planets;
    }

    /**
     * @return Core immutable configuration asset supplying fundamental physical stellar metrics
     */
    public StarSystemAsset getStaticData() {
        return staticInfo;
    }
}