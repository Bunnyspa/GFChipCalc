/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.puzzle;

import java.awt.Point;
import main.util.IO;

/**
 *
 * @author Bunnyspa
 */
public class Puzzle implements Comparable<Puzzle> {

    public final Shape shape;
    public final int rotation;
    public final Point location;

    public Puzzle(Shape shape, int rotation, Point location) {
        this.shape = shape;
        this.rotation = rotation;
        this.location = location;
    }

    public static int compareLocation(Point o1, Point o2) {
        int xCompare = Integer.compare(o1.x, o2.x);
        if (xCompare != 0) {
            return xCompare;
        }

        return Integer.compare(o1.y, o2.y);
    }

    @Override
    public int compareTo(Puzzle o) {
        int shapeCompare = Shape.compare(shape, o.shape);
        if (shapeCompare != 0) {
            return shapeCompare;
        }
        return compareLocation(location, o.location);
    }

    @Override
    public String toString() {
        return "{" + shape + ", " + rotation + ", " + IO.data(location) + "}";
    }
}
