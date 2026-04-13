package ca.dauqui.spacecolonygame.gamedata.starsystem;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class PlanetAsset {
    private final Map<StarDataColumn, String> data;

    public PlanetAsset(String[] dataStr){
        var temp = new EnumMap<StarDataColumn, String>(StarDataColumn.class);

        for (StarDataColumn s : StarDataColumn.values()) {
            temp.put(s, dataStr[s.ordinal()].trim());
        }

        temp.put(StarDataColumn.SPEC, temp.get(StarDataColumn.SPEC).replace(" ", "-"));
        data = Collections.unmodifiableMap(temp);
    }

    public double getTeff() {
        if (!"".equals(data.get(StarDataColumn.TEFF))) {
            return Double.parseDouble(data.get(StarDataColumn.TEFF).replace("?", "").trim());
        }  else {
            return -1.0;
        }
    }

    public String getType() {
        return data.get(StarDataColumn.TY);
    }
}