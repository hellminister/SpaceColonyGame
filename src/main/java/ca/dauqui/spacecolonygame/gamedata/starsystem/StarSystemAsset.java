package ca.dauqui.spacecolonygame.gamedata.starsystem;

import ca.dauqui.spacecolonygame.localization.Localize;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.geometry.Point3D;

import java.util.*;


public class StarSystemAsset {
    private static final String STAR_SYSTEM_ASSET_LOCALIZE_PATH = "data/starsystems/";
    private final Localize<Target> translatableStringValues;
    private final List<PlanetAsset> planetAssets;
    private final Map<StarDataColumn, String> data;
    private final Point3D coordinate;


    public StarSystemAsset(String[] dataStr){
        translatableStringValues = new Localize<>(Target.class, STAR_SYSTEM_ASSET_LOCALIZE_PATH + dataStr[0].trim() + ".txt");

        var temp = new EnumMap<StarDataColumn, String>(StarDataColumn.class);

        for (StarDataColumn s : StarDataColumn.values()) {
            temp.put(s, dataStr[s.ordinal()].trim());
        }

        temp.put(StarDataColumn.SPEC, temp.get(StarDataColumn.SPEC).replace(" ", "-"));
        data = Collections.unmodifiableMap(temp);

        coordinate = new Point3D(Double.parseDouble(data.get(StarDataColumn.X)), Double.parseDouble(data.get(StarDataColumn.Y)), Double.parseDouble(data.get(StarDataColumn.Z)));

        planetAssets = new LinkedList<>();
    }


    public ReadOnlyStringProperty nameProperty() {
        return translatableStringValues.getString(Target.NAME);
    }

    public ReadOnlyStringProperty descriptionProperty() {
        return translatableStringValues.getString(Target.DESCRIPTION);
    }

    public double getDistance(){
        return coordinate.distance(0,0,0);
    }

    public int getMinNumberOfPlanets() {
        if (!"".equals(data.get(StarDataColumn.MINPLN))) {
            return Integer.parseInt(data.get(StarDataColumn.MINPLN).trim());
        }  else {
            return -1;
        }
    }

    public int getMaxNumberOfPlanets() {
        if (!"".equals(data.get(StarDataColumn.MAXPLN))) {
            return Integer.parseInt(data.get(StarDataColumn.MAXPLN).trim());
        }  else {
            return -1;
        }
    }

    public void addPlanetAsset(PlanetAsset planet) {
        planetAssets.add(planet);
    }

    public String getSpectralType() {
        return data.get(StarDataColumn.SPEC);
    }

    public double getMass() {
        if (!"".equals(data.get(StarDataColumn.MASS))) {
            return Double.parseDouble(data.get(StarDataColumn.MASS).replace("?", "").trim());
        }  else {
            return -1.0;
        }
    }

    public Point3D getCoordinate() {
        return coordinate;
    }

    public double getRadius() {
        if (!"".equals(data.get(StarDataColumn.RADIUS))) {
            return Double.parseDouble(data.get(StarDataColumn.RADIUS).replace("?", "").trim());
        }  else {
            return -1.0;
        }
    }

    public double getTeff() {
        if (!"".equals(data.get(StarDataColumn.TEFF))) {
            return Double.parseDouble(data.get(StarDataColumn.TEFF).replace("?", "").trim());
        }  else {
            return -1.0;
        }
    }

    public double getVisualLum() {
        if (!"".equals(data.get(StarDataColumn.VIS_LUM))) {
            return Double.parseDouble(data.get(StarDataColumn.VIS_LUM).replace("?", "").trim());
        }  else {
            return -1.0;
        }
    }

    public Double getBolometricLum() {
        if (!"".equals(data.get(StarDataColumn.BOL_LUM))) {
            return Double.parseDouble(data.get(StarDataColumn.BOL_LUM).replace("?", "").trim());
        }  else {
            return -1.0;
        }
    }

    public List<PlanetAsset> getKnownPlanets() {
        return planetAssets;
    }

    public enum Target{
        NAME,
        DESCRIPTION,
    }
}