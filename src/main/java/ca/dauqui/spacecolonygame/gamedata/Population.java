package ca.dauqui.spacecolonygame.gamedata;

import ca.dauqui.spacecolonygame.gamedata.tiledata.Building;
import ca.dauqui.spacecolonygame.localization.Localizable;
import ca.dauqui.spacecolonygame.localization.Localize;
import ca.dauqui.spacecolonygame.localization.Localized;
import javafx.beans.property.ReadOnlyStringProperty;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public class Population {
    private final EnumSet<Tag> tags;
    private int age;
    private double maxHealth;
    private double health;
    private double morale;
    private Building attachedTo;

    public Population() {
        tags = EnumSet.noneOf(Tag.class);
    }

    public Set<Tag> getTags() {
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

    @Localized
    public enum Tag implements Localizable {
        BABY,
        STUDENT,
        WORKER,
        SCIENTIST,
        RETIRED,
        MALE,
        FEMALE,
        PREGNANT,
        STERILE,
        ROBOT,
        ;

        static {
            BABY.incompatible.addAll(Arrays.asList(STUDENT, WORKER, SCIENTIST, PREGNANT, ROBOT));
            STUDENT.incompatible.addAll(Arrays.asList(WORKER, SCIENTIST, BABY));
            WORKER.incompatible.addAll(Arrays.asList(BABY, RETIRED));
            SCIENTIST.incompatible.addAll(Arrays.asList(BABY, RETIRED));
            RETIRED.incompatible.addAll(Arrays.asList(BABY, WORKER, SCIENTIST));
            FEMALE.incompatible.addAll(Arrays.asList(MALE, ROBOT));
            MALE.incompatible.addAll(Arrays.asList(FEMALE, ROBOT));
            PREGNANT.incompatible.addAll(Arrays.asList(MALE, STERILE, ROBOT));
            STERILE.incompatible.addAll(Arrays.asList(PREGNANT));
            ROBOT.incompatible.addAll(Arrays.asList(BABY, MALE, FEMALE, PREGNANT));
        }

        private final EnumSet<Tag> incompatible;
        Tag() {
            incompatible = EnumSet.noneOf(Tag.class);
        }

        private static final String LOCALIZATION_BUNDLE_PATH = "ca.dauqui.spacecolonygame.localization.enums";

        @Override
        public ReadOnlyStringProperty localizedProperty() {
            return Localize.getProperty(LOCALIZATION_BUNDLE_PATH, "PopulationTag." + name());
        }
        @Override
        public String toString() {
            return Localize.getProperty(LOCALIZATION_BUNDLE_PATH, "PopulationTag." + name()).get();
        }

        public boolean isCompatible(Tag tag) {
            return !incompatible.contains(tag);
        }
    }

}