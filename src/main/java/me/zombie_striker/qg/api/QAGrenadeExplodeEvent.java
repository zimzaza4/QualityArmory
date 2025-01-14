package me.zombie_striker.qg.api;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class QAGrenadeExplodeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private Entity thrower;
    private List<Entity> victims;
    private final Location location;
    private boolean cancel = false;

    public QAGrenadeExplodeEvent(Location location, Entity thrower, List<Entity> victims) {
        this.location = location;
        this.thrower = thrower;
        this.victims = victims;
    }


    public Location getLocation() {
        return location;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean canceled) {
        this.cancel = canceled;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Entity getThrower() {
        return thrower;
    }

    public void setThrower(Entity thrower) {
        this.thrower = thrower;
    }

    public List<Entity> getVictims() {
        return victims;
    }

    public void setVictims(List<Entity> victims) {
        this.victims = victims;
    }

}
