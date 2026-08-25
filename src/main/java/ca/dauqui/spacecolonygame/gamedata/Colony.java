package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.gamedata.starsystem.Planet;
import ca.dauqui.spacecolonygame.gamedata.starsystem.StarSystem;

public class Colony {
    private String colonyName;
    private String commanderName;
    private String aiName;
    private boolean saved;
    private StarSystem starSystem;
    private Planet planet;
    private Site site;

    public Colony(){
        saved = false;
    }




    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setCommanderName(String commanderName) {
        this.commanderName = commanderName;
    }

    public void setAIName(String aiName) {
        this.aiName = aiName;
    }

    public void setPlanet(Planet planet) {
        this.planet = planet;
    }

    public void setSite(Site site) {
        this.site = site;
    }
    public void setStarSystem(StarSystem starSystem) {
        this.starSystem = starSystem;
    }
}