package org.skvelo.elements;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

public class EffVelocity extends Effect {

    private Expression<Entity> entityExpr;
    private Expression<Entity> targetEntity;
    private Expression<Location> targetLocation;
    private Expression<Number> speedExpr;
    private Expression<Number> x, y, z;
    private String direction;
    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
        entityExpr = (Expression<Entity>) exprs[0];
        pattern = matchedPattern;

        switch (matchedPattern) {
            case 0 -> { // direction
                direction = parseResult.regexes.get(0).group();
                speedExpr = (Expression<Number>) exprs[1];
            }
            case 1 -> { // towards entity
                targetEntity = (Expression<Entity>) exprs[1];
                speedExpr = (Expression<Number>) exprs[2];
            }
            case 2 -> { // towards location
                targetLocation = (Expression<Location>) exprs[1];
                speedExpr = (Expression<Number>) exprs[2];
            }
            case 3 -> { // away from entity
                targetEntity = (Expression<Entity>) exprs[1];
                speedExpr = (Expression<Number>) exprs[2];
            }
            case 4 -> { // away from location
                targetLocation = (Expression<Location>) exprs[1];
                speedExpr = (Expression<Number>) exprs[2];
            }
            case 5 -> { // custom vector
                x = (Expression<Number>) exprs[1];
                y = (Expression<Number>) exprs[2];
                z = (Expression<Number>) exprs[3];
                speedExpr = (Expression<Number>) exprs[4];
            }
        }
        return true;
    }

    @Override
    protected void execute(Event e) {
        Entity ent = entityExpr.getSingle(e);
        if (ent == null) return;

        double speed = speedExpr != null && speedExpr.getSingle(e) != null
                ? speedExpr.getSingle(e).doubleValue() : 1.0;

        Vector vec = new Vector();

        try {
            switch (pattern) {
                case 0 -> { // direction
                    Location loc = ent.getLocation();
                    switch (direction.toLowerCase()) {
                        case "north" -> vec.setZ(-speed);
                        case "south" -> vec.setZ(speed);
                        case "east" -> vec.setX(speed);
                        case "west" -> vec.setX(-speed);
                        case "up" -> vec.setY(speed);
                        case "down" -> vec.setY(-speed);
                        case "forward" -> vec = loc.getDirection().normalize().multiply(speed);
                        case "backward" -> vec = loc.getDirection().normalize().multiply(-speed);
                        case "left" -> {
                            Vector d = loc.getDirection().normalize();
                            vec.setX(-d.getZ() * speed);
                            vec.setZ(d.getX() * speed);
                        }
                        case "right" -> {
                            Vector d = loc.getDirection().normalize();
                            vec.setX(d.getZ() * speed);
                            vec.setZ(-d.getX() * speed);
                        }
                    }
                }
                case 1 -> { // towards entity
                    Entity target = targetEntity.getSingle(e);
                    if (target == null) return;
                    vec = target.getLocation().toVector().subtract(ent.getLocation().toVector());
                }
                case 2 -> { // towards location
                    Location target = targetLocation.getSingle(e);
                    if (target == null) return;
                    vec = target.toVector().subtract(ent.getLocation().toVector());
                }
                case 3 -> { // away from entity
                    Entity target = targetEntity.getSingle(e);
                    if (target == null) return;
                    vec = ent.getLocation().toVector().subtract(target.getLocation().toVector());
                }
                case 4 -> { // away from location
                    Location target = targetLocation.getSingle(e);
                    if (target == null) return;
                    vec = ent.getLocation().toVector().subtract(target.toVector());
                }
                case 5 -> { // custom vector
                    double vx = x.getSingle(e).doubleValue();
                    double vy = y.getSingle(e).doubleValue();
                    double vz = z.getSingle(e).doubleValue();
                    vec = new Vector(vx, vy, vz);
                }
            }

            // ✅ Fix: check for zero or invalid vectors before normalizing
            if (vec.lengthSquared() == 0 || !Double.isFinite(vec.getX()) || !Double.isFinite(vec.getY()) || !Double.isFinite(vec.getZ())) {
                return;
            }

            vec.normalize().multiply(speed);
            ent.setVelocity(vec);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "velocity effect";
    }
}
