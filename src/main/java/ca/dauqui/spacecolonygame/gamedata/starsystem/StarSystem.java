package ca.dauqui.spacecolonygame.gamedata.starsystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class StarSystem {
    private final StarSystemAsset staticInfo;
    private final List<Planet> planets;
    private static final Random random = new Random();

    public StarSystem(StarSystemAsset probed) {
        this.staticInfo = probed;
        planets = new ArrayList<>();
        PlanetGenerator.start();

        for (PlanetAsset ps : probed.getKnownPlanets()){
            Planet planet = PlanetGenerator.generate(probed, ps);
            planets.add(planet);
        }

        int min = Math.max(probed.getMinNumberOfPlanets(), 0);
        int max = Math.max(probed.getMaxNumberOfPlanets(), 2);

        int addPlanets = random.nextInt(min, max+1);

        for (int i = 0; i < addPlanets; i++) {
            Planet planet = PlanetGenerator.generate(probed);
            planets.add(planet);
        }

        planets.sort(Comparator.comparingDouble(Planet::getDistance));

    }

    public List<Planet> getPlanets() {
        return planets;
    }

    public double getHabitableProbability() {
        return 0.5;
    }

    public StarSystemAsset getStaticData() {
        return staticInfo;
    }
}