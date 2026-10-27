package ca.dauqui.spacecolonygame.gamedata.tiledata;

import ca.dauqui.spacecolonygame.gamedata.Population;
import ca.dauqui.spacecolonygame.gamedata.PopulationTag;

import java.util.EnumSet;
import java.util.Set;

public class PopulationSlot {
    private Set<PopulationTag> requiredTags;
    private Set<PopulationTag> prohibitedTags;
    private Population filledBy;


}