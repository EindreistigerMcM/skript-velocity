package org.skvelo.elements;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;

public class EffVelocity extends Effect {

    private Expression<Entity> entityExpr;
    private Expression<?> target;
    private Expression<Number> speedExpr;
    private Expression<Number> x, y, z;
    private String direction;
    private int pattern;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
        entityExpr = (Expression<Entity>) exprs[0];
        pattern = matchedPattern;

        switch (matchedPattern) {
            case 0 -> { direction = "north"; speedExpr = (Expression<Number>) exprs[1]; }
            case 1 -> { direction = "south"; speedExpr = (Expression<Number>) exprs[1]; }
            case 2 -> { direction = "east";  speedExpr = (Expression<Number>) exprs[1]; }
            case 3 -> { direction = "west";  speedExpr = (Expression<Number>) exprs[1]; }
            case 4 -> { direction = "up";    speedExpr = (Expression<Number>) exprs[1]; }
            case 5 -> { direction = "down";  speedExpr = (Expression<Number>) exprs[1]; }
            case 6 -> { direction = "forward"; speedExpr = (Expression<Number>) exprs[1]; }
            case 7 -> { direction = "backward"; speedExpr = (Expression<Number>) exprs[1]; }
            case 8 -> { direction = "left";  speedExpr = (Expression<Number>) exprs[1]; }
            case 9 -> { direction = "right"; speedExpr = (Expression<Number>) exprs[1]; }

            case 10, 11, 12, 13 -> {
                target = exprs[1];
                speedExpr = (Expression<Number>) exprs[2];
            }

            case 14 -> {
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

        double speed = 1.0;
        if (speedExpr != null && speedExpr.getSingle(e) != null)
            speed = speedExpr.getSingle(e).doubleValue();

        Vector vec = new Vector();

        try {
            switch (pattern) {
                // directions (0..9)
                case 0,1,2,3,4,5,6,7,8,9 -> {
                    Location loc = ent.getLocation();
                    switch (direction) {
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

                case 10, 11 -> {
                    Location targetLoc = resolveLocationRobust(e, target);
                    if (targetLoc == null) {
                        Bukkit.getLogger().info("[SkVelo] Could not resolve target location (towards).");
                        return;
                    }
                    vec = targetLoc.toVector().subtract(ent.getLocation().toVector());
                }

                case 12, 13 -> {
                    Location targetLoc = resolveLocationRobust(e, target);
                    if (targetLoc == null) {
                        Bukkit.getLogger().info("[SkVelo] Could not resolve target location (away).");
                        return;
                    }
                    vec = ent.getLocation().toVector().subtract(targetLoc.toVector());
                }

                case 14 -> {
                    double vx = x.getSingle(e).doubleValue();
                    double vy = y.getSingle(e).doubleValue();
                    double vz = z.getSingle(e).doubleValue();
                    vec = new Vector(vx, vy, vz);
                }
            }

            if (vec.lengthSquared() == 0) return;
            vec = vec.normalize().multiply(speed);
            if (!Double.isFinite(vec.getX()) || !Double.isFinite(vec.getY()) || !Double.isFinite(vec.getZ())) {
                Bukkit.getLogger().warning("[SkVelo] Computed vector not finite; skipping.");
                return;
            }
            ent.setVelocity(vec);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private Location resolveLocationRobust(Event e, Expression<?> expr) {
        try {

            Object raw = expr.getSingle(e);
            if (raw != null) {
                Location direct = parseIfLocationLike(raw);
                if (direct != null) return direct;
            }

            String repr = expr.toString(e, false);
            String cleaned = cleanupVarName(repr);

            String[] candidates = new String[] {
                    cleaned,
                    cleaned.replaceAll("^\\{+|\\}+$", ""),
                    cleaned.replaceAll("^%+|%+$", ""),
                    "{" + cleaned + "}",
                    "{" + cleaned + "}" .replace(" ", ""),
                    cleaned + "::0",
                    cleaned + "::*"
            };

            for (String cand : candidates) {
                if (cand == null || cand.isEmpty()) continue;
                Object v = Variables.getVariable(cand, e, false);
                if (v != null) {
                    Location fromVar = parseIfLocationLike(v);
                    if (fromVar != null) {
                        return fromVar;
                    }
                }
                Object gv = Variables.getVariable(cand, e, true);
                if (gv != null) {
                    Location fromVar = parseIfLocationLike(gv);
                    if (fromVar != null) {
                        return fromVar;
                    }
                }
            }

            String[] fallbackNames = new String[] {"loc", "_loc", "location", "my.loc", "myloc", "spawn", "targetloc"};
            for (String n : fallbackNames) {
                Object v = Variables.getVariable(n, e, false);
                if (v != null) {
                    Location fromVar = parseIfLocationLike(v);
                    if (fromVar != null) {
                        return fromVar;
                    }
                }
                Object gv = Variables.getVariable(n, e, true);
                if (gv != null) {
                    Location fromVar = parseIfLocationLike(gv);
                    if (fromVar != null) {
                        return fromVar;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Location parseIfLocationLike(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Location loc) {
            if (loc.getWorld() == null && !Bukkit.getWorlds().isEmpty())
                loc.setWorld(Bukkit.getWorlds().get(0));
            return loc;
        }
        if (raw instanceof Entity ent) {
            return ent.getLocation();
        }
        if (raw instanceof String s) {
            try {
                String a = s;
                if (a.startsWith("\"") && a.endsWith("\"")) a = a.substring(1, a.length()-1);
                a = a.replaceAll("\\s*,\\s*", ",").trim();
                String world = null;
                if (a.contains("in '")) {
                    int idx = a.indexOf("in '");
                    int start = idx + 4;
                    int end = a.indexOf("'", start);
                    if (end > start) world = a.substring(start, end);
                    a = a.substring(0, idx).trim();
                } else if (a.contains(" in ")) {
                    int idx = a.indexOf(" in ");
                    world = a.substring(idx + 4).trim();
                    a = a.substring(0, idx).trim();
                }

                double x = 0, y = 0, z = 0;
                float yaw = 0, pitch = 0;

                String[] parts = a.split(",");
                for (String p : parts) {
                    String[] kv = p.split(":");
                    if (kv.length < 2) continue;
                    String key = kv[0].trim().toLowerCase();
                    String val = kv[1].trim();
                    if (kv.length > 2) val = p.substring(p.indexOf(":") + 1).trim();
                    switch (key) {
                        case "x" -> x = Double.parseDouble(val);
                        case "y" -> y = Double.parseDouble(val);
                        case "z" -> z = Double.parseDouble(val);
                        case "yaw" -> yaw = Float.parseFloat(val);
                        case "pitch" -> pitch = Float.parseFloat(val);
                    }
                }
                if (world == null || world.isEmpty()) {
                    if (!Bukkit.getWorlds().isEmpty()) world = Bukkit.getWorlds().get(0).getName();
                    else return null;
                }
                Location loc = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
                return loc;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String cleanupVarName(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        s = s.replaceAll("[^A-Za-z0-9_:\\*\\.]","").trim();
        return s;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "velocity effect";
    }
}
