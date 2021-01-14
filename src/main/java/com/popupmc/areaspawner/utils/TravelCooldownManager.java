package com.popupmc.areaspawner.utils;

import com.popupmc.areaspawner.AreaSpawner;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.concurrent.TimeUnit;

public class TravelCooldownManager {

    private static TravelCooldownManager instance;

    final private AreaSpawner plugin;


    private TravelCooldownManager(AreaSpawner plugin){
        this.plugin = plugin;
    }


    /**
     * Returns the amount of milliseconds the given player has to wait before traveling again.
     * @param playerName The player to look for.
     * @return The milliseconds left for the player to travel again.
     */
    public long getTimeLeft(String playerName){
        FileConfiguration cooldownFile = plugin.getCooldownYaml().getAccess();

        if(cooldownFile.contains("cooldown."+playerName)){
            long startTime = cooldownFile.getLong("cooldown."+playerName);
            long cooldownMillis = TimeUnit.SECONDS.toMillis(Settings.getInstance().getTravelCooldown()/20);
            return (startTime + cooldownMillis) - System.currentTimeMillis();
        }
        return 0;
    }

    /**
     * Adds a player to the cooldown setting in the cooldown file the current system's time in milliseconds.
     * @param playerName The player to add to the cooldown.
     */
    public void addToCooldown(String playerName){
        FileConfiguration cooldownFile = plugin.getCooldownYaml().getAccess();
        cooldownFile.set("cooldown."+playerName, System.currentTimeMillis());
        plugin.getCooldownYaml().save();
    }


    public static void createInstance(AreaSpawner plugin){
        if(instance == null) instance = new TravelCooldownManager(plugin);
    }

    public static TravelCooldownManager getInstance(){
        return instance;
    }
}
