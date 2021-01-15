package com.popupmc.areaspawner.utils;

import com.popupmc.areaspawner.AreaSpawner;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.concurrent.TimeUnit;

/**
 * Class for managing players that are on cooldown and checking if a player is on cooldown.
 *
 * @author lelesape
 */
public class TravelCooldownManager {

    /**
     * The only instance for this class.
     */
    private static TravelCooldownManager instance;

    /**
     * AreaSpawner's main class instance.
     */
    final private AreaSpawner plugin;

    /**
     * Creates an instance of this manager.
     * @param plugin AreaSpawner's main class instance.
     */
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

    /**
     * Creates an instance of TravelCooldownManager if none found.
     * @param plugin AreaSpawner's main class instance.
     */
    public static void createInstance(AreaSpawner plugin){
        if(instance == null) instance = new TravelCooldownManager(plugin);
    }

    /**
     * Gets the single instance of this class. {@link #createInstance(AreaSpawner)} should be run first.
     * @return An instance of this TravelCooldownManager and the only one in existence.
     */
    public static TravelCooldownManager getInstance(){
        return instance;
    }
}
