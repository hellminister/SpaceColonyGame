package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.gamedata.starsystem.PlanetAsset;
import ca.dauqui.spacecolonygame.gamedata.starsystem.StarDataColumn;
import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystemAsset;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class StaticDataAssets {
    private static final Logger LOG = Logger.getLogger(StaticDataAssets.class.getName());

    private static final String STAR_DATA_PATH = "src/main/resources/ca/dauqui/spacecolonygame/data/starsystems/";
    private static final  double MAX_DISTANCE = 20.0;


    private final List<StarSystemAsset> starSystemAssets;

    public StaticDataAssets() {
        var starListTemp = new ArrayList<StarSystemAsset>();

        loadStarData(starListTemp);

        starSystemAssets = Collections.unmodifiableList(starListTemp);
    }

    private static void loadStarData(ArrayList<StarSystemAsset> starListTemp) {
        String line = "not started";
        Path path = new File(STAR_DATA_PATH + "StarData.txt").toPath();
        try (BufferedReader br = Files.newBufferedReader(path)) {
            line = br.readLine();

            StarSystemAsset last = null;
            while (line != null) {
                if (!line.isBlank() && !line.startsWith("#")) {
                    String[] text = line.concat(" ").split(",");

                    if (Double.parseDouble(text[StarDataColumn.DIST.ordinal()]) > MAX_DISTANCE){
                        break;
                    }
                    last = treatStellarBody(text, last, starListTemp);

                }

                line = br.readLine();
            }

        } catch (IOException _) {
            LOG.severe(() -> "Error loading localization file : " + path);
        } catch (Exception e) {
            String finalLine = line;
            LOG.warning(() -> "tries to do something with " + finalLine + "\n" + e.getMessage());
        }
    }

    private static StarSystemAsset treatStellarBody(String[] text, StarSystemAsset last, List<StarSystemAsset> starListTemp) {
        String type = text[StarDataColumn.TY.ordinal()].trim().replace("?", "");

        switch (type) {
            case "S":
                var last2 = new StarSystemAsset(text);
                starListTemp.add(last2);
                return last2;
            case "PJ", "PN", "PT":
                var planet = new PlanetAsset(text);
                if (last != null) {
                    last.addPlanetAsset(planet);
                } else {
                    LOG.severe(() -> "No Star is yet Created or previous non-planet object is not a main sequence star -> " + Arrays.toString(text));
                }
                return last;
            default:
                LOG.info(() -> "Skipping " + text[StarDataColumn.NAME.ordinal()].trim() + " of type " + type);
                return last;
        }
    }

    public List<StarSystemAsset> getStarSystems() {
        return starSystemAssets;
    }
}