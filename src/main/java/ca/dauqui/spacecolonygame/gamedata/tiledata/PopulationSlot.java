package ca.dauqui.spacecolonygame.gamedata.tiledata;

import ca.dauqui.spacecolonygame.gamedata.Population;
import ca.dauqui.spacecolonygame.gamedata.PopulationTag;

import java.util.Set;

/**
 * A population slot to be filled in a building
 */
public class PopulationSlot {
    private Set<PopulationTag> requiredTags;
    private Set<PopulationTag> prohibitedTags;
    private Population filledBy;


}