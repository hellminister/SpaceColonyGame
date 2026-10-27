package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.gamedata.tiledata.Building;

import java.util.HashSet;
import java.util.Set;

public class Population {
    private final Set<PopulationTag> tags;
    private int age;
    private double maxHealth;
    private double health;
    private double morale;
    private Building attachedTo;

    public Population() {
        tags = new HashSet<>();
    }

    public Set<PopulationTag> getTags() {
        return tags;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double getMorale() {
        return morale;
    }

    public void setMorale(double morale) {
        this.morale = morale;
    }

}