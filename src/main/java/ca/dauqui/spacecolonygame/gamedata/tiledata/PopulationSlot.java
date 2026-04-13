package ca.dauqui.spacecolonygame.gamedata.tiledata;

import ca.dauqui.spacecolonygame.gamedata.Population;

import java.util.EnumSet;

public class PopulationSlot {
    private EnumSet<Population.Tag> requiredTags;
    private EnumSet<Population.Tag> prohibitedTags;
    private Population filledBy;


}