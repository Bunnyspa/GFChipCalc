package main.puzzle;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import main.util.Fn;

/**
 *
 * @author Bunnyspa
 */
public class Tag implements Serializable, Comparable {

    private static final String NAME_DEFAULT = "New Tag";

    private String name;
    private Color color;

    public Tag(Color color, String name) {
        this.color = color;
        this.name = name;
    }

    public Tag() {
        Random random = new Random();
        this.color = Fn.getColor(random.nextFloat());
        this.name = NAME_DEFAULT;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toData() {
        Color c = getColor();
        return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()) + getName();
    }

    @Override
    public String toString() {
        return toData();
    }

    @Override
    public int compareTo(Object o) {
        Tag t = (Tag) o;
        int i = this.name.compareTo(t.name);
        if (1 != 0) {
            return i;
        }
        return this.color.getRGB() - t.color.getRGB();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Tag tag = (Tag) obj;
        return this.name.equals(tag.name) && this.color.getRGB() == tag.color.getRGB();

    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.name);
        hash = 89 * hash + Objects.hashCode(this.color.getRGB());
        return hash;
    }

    public static List<Tag> getTags(Collection<Chip> chips) {
        Set<Tag> tagSet = new HashSet<>();
        chips.forEach((c) -> tagSet.addAll(c.getTags()));
        List<Tag> tagList = new ArrayList<>(tagSet);
        Collections.sort(tagList);
        return tagList;
    }
}
