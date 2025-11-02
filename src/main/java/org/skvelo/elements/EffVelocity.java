package org.skvelo.elements;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.bukkit.Bukkit;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

public class EffVelocity extends Effect {

    private Expression<Entity> entity;
    private Expression<Entity> target;
    private Expression<Number> speed;
    private String direction;
    private boolean isTowards, isAway;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
        entity = (Expression<Entity>) exprs[0];
        speed = (Expression<Number>) exprs[exprs.length - 1];

        if (matchedPattern == 0) {
            int mark = parseResult.mark;
            direction = switch (mark) {
                case 1 -> "north";
                case 2 -> "south";
                case 3 -> "east";
                case 4 -> "west";
                case 5 -> "up";
                case 6 -> "down";
                case 7 -> "forward";
                case 8 -> "backward";
                case 9 -> "left";
                case 10 -> "right";
                default -> "north";
            };
        } else if (matchedPattern == 1) {
            target = (Expression<Entity>) exprs[1];
            isTowards = true;
        } else if (matchedPattern == 2) {
            target = (Expression<Entity>) exprs[1];
            isAway = true;
        }

        return true;
    }

    @Override
    protected void execute(org.bukkit.event.Event e) {
        Entity ent = entity.getSingle(e);
        if (ent == null) return;
        double spd = speed.getSingle(e).doubleValue();

        Vector velocity = new Vector(0, 0, 0);

        if (isTowards || isAway) {
            Entity tgt = target.getSingle(e);
            if (tgt == null) return;

            Vector diff = tgt.getLocation().toVector().subtract(ent.getLocation().toVector());
            if (diff.lengthSquared() == 0) return;

            diff.normalize();
            if (isAway) diff.multiply(-1);

            velocity = diff.multiply(spd);

        } else {
            switch (direction.toLowerCase()) {
                case "north" -> velocity = new Vector(0, 0, -1);
                case "south" -> velocity = new Vector(0, 0, 1);
                case "east" -> velocity = new Vector(1, 0, 0);
                case "west" -> velocity = new Vector(-1, 0, 0);
                case "up" -> velocity = new Vector(0, 1, 0);
                case "down" -> velocity = new Vector(0, -1, 0);
                case "forward", "backward", "left", "right" -> {
                    if (ent instanceof LivingEntity living) {
                        Vector dir = living.getLocation().getDirection().normalize();
                        if (direction.equalsIgnoreCase("backward")) dir.multiply(-1);
                        else if (direction.equalsIgnoreCase("left")) dir.rotateAroundY(-Math.PI / 2);
                        else if (direction.equalsIgnoreCase("right")) dir.rotateAroundY(Math.PI / 2);
                        velocity = dir;
                    }
                }
            }
        }

        ent.setVelocity(velocity.normalize().multiply(spd));
    }

    @Override
    public String toString(org.bukkit.event.Event e, boolean debug) {
        return "set velocity of " + entity.toString(e, debug) + " to " + direction + " at speed " + speed.toString(e, debug);
    }
}
