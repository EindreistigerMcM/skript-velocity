package org.skvelo;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import org.skvelo.elements.EffVelocity;

public class Main extends JavaPlugin {

    private static Main instance;
    private SkriptAddon addon;

    @Override
    public void onEnable() {
        instance = this;

        try {
            addon = Skript.registerAddon(this);
            addon.loadClasses("org.skvelo", "elements");
            Bukkit.getLogger().info("[SkVelo] Classes loaded successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }


        Skript.registerEffect(EffVelocity.class,
                "set velocity of %entity% to (north|south|east|west|up|down|forward|backward|left|right) at speed %number%",
                "set velocity of %entity% towards %entity% at speed %number%",
                "set velocity of %entity% towards %object% at speed %number%",
                "set velocity of %entity% away from %entity% at speed %number%",
                "set velocity of %entity% away from %object% at speed %number%",
                "set velocity of %entity% to vector %number%, %number%, %number% at speed %number%"
        );




        Bukkit.getLogger().info("[SkVelo] Enabled!");
    }

    public static Main getInstance() {
        return instance;
    }

    public SkriptAddon getAddonInstance() {
        return addon;
    }
}
