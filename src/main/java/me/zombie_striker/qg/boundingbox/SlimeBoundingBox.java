package me.zombie_striker.qg.boundingbox;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class SlimeBoundingBox implements AbstractBoundingBox {
    @Override
    public boolean intersects(Entity shooter, Location check, Entity base) {
        if(intersectsBody(check, base))
            return true;
        return intersectsHead(check,base);
    }

    @Override
    public boolean allowsHeadshots() {
        return false;
    }

    @Override
    public boolean intersectsBody(Location check, Entity base) {
        return base.getBoundingBox().contains(check.toVector());
    }

    @Override
    public boolean intersectsHead(Location check, Entity base) {
        return false;
    }


    @Override
    public double maximumCheckingDistance(Entity base) {
        return (base.getBoundingBox().getWidthX() + base.getBoundingBox().getWidthZ())*2;
    }
}
