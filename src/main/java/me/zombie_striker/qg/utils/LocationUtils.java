package me.zombie_striker.qg.utils;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class LocationUtils {
    public static List<Location> line(Location locAO, Location locBO, double rate) {
        Location locA = locAO.clone();
        Location locB = locBO.clone();
        rate = Math.abs(rate);
        Vector vectorAB = locB.clone().subtract(locA).toVector();
        double vectorLength = vectorAB.length();
        vectorAB.normalize();
        List<Location> points = new ArrayList<>();
        for (double i = 0; i <= vectorLength; i += rate) {
            Vector vector = vectorAB.clone().multiply(i);
            locA.add(vector);
            points.add(locA.clone());
            locA.subtract(vector);
        }

        return points;
    }
}
