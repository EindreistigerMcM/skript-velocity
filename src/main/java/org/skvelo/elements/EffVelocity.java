package org.skvelo.elements;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.event.Event;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

public class EffVelocity extends Effect {

    private Expression<Entity> entity;
    private Expression<Entity> target;
    private Expression<Number> speed;
    private Expression<Number> x, y, z; // custom vector components

    private String direction;
    private boolean isTowards, isAway, isCustom;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
        entity = (Expression<Entity>) exprs[0];

        switch (matchedPattern) {
            case 0 -> { // direction
                direction = parseResult.regexes.get(0).group(); // or simpler: just store the direction as a string
                speed = (Expression<Number>) exprs[1];
            }
            case 1 -> { // towards
                target = (Expression<Entity>) exprs[1];
                speed = (Expression<Number>) exprs[2];
                isTowards = true;
            }
            case 2 -> { // away
                target = (Expression<Entity>) exprs[1];
                speed = (Expression<Number>) exprs[2];
                isAway = true;
            }
            case 3 -> { // custom vector
                x = (Expression<Number>) exprs[1];
                y = (Expression<Number>) exprs[2];
                z = (Expression<Number>) exprs[3];
                speed = (Expression<Number>) exprs[4];
                isCustom = true;
            }
        }

        return true;
    }


    @Override
    protected void execute(Event e) {
        Entity ent = entity.getSingle(e);
        if (ent == null) return;

        double spd = speed != null && speed.getSingle(e) != null ? speed.getSingle(e).doubleValue() : 1.0;
        Vector vec = new Vector(0, 0, 0);
        Location loc = ent.getLocation();

        try {
            if (isTowards || isAway) {
                Entity tgt = target.getSingle(e);
                if (tgt == null) return;
                vec = tgt.getLocation().toVector().subtract(ent.getLocation().toVector());
                if (vec.lengthSquared() == 0) return;
                vec.normalize().multiply(spd);
                if (isAway) vec.multiply(-1);
            } else if (isCustom) {
                double vx = x.getSingle(e).doubleValue();
                double vy = y.getSingle(e).doubleValue();
                double vz = z.getSingle(e).doubleValue();
                vec = new Vector(vx, vy, vz).multiply(spd);
            } else {
                switch (direction.toLowerCase()) {
                    case "north" -> vec.setZ(-spd);
                    case "south" -> vec.setZ(spd);
                    case "east" -> vec.setX(spd);
                    case "west" -> vec.setX(-spd);
                    case "up" -> vec.setY(spd);
                    case "down" -> vec.setY(-spd);
                    case "forward" -> vec = loc.getDirection().normalize().multiply(spd);
                    case "backward" -> vec = loc.getDirection().normalize().multiply(-spd);
                    case "left" -> {
                        Vector d = loc.getDirection().normalize();
                        vec.setX(-d.getZ() * spd);
                        vec.setZ(d.getX() * spd);
                    }
                    case "right" -> {
                        Vector d = loc.getDirection().normalize();
                        vec.setX(d.getZ() * spd);
                        vec.setZ(-d.getX() * spd);
                    }
                }
            }

            if (Double.isFinite(vec.getX()) && Double.isFinite(vec.getY()) && Double.isFinite(vec.getZ()))
                ent.setVelocity(vec);
        } catch (Exception ignored) {
        }
    }

    @Override
    public String toString(Event e, boolean debug) {
        if (isCustom) return "velocity " + entity + " vector " + x + "," + y + "," + z + " speed " + speed;
        if (isTowards) return "velocity " + entity + " towards " + target + " " + speed;
        if (isAway) return "velocity " + entity + " away from " + target + " " + speed;
        return "velocity " + entity + " " + direction + " " + speed;
    }
}
