/*
Copyright 2020 Leandro Alfonso

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.popupmc.areaspawner.spawn;

import com.popupmc.areaspawner.AreaSpawner;
import com.popupmc.areaspawner.utils.Settings;
import com.popupmc.areaspawner.utils.ConsoleLogger;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Creates new random spawn points with config parameters, saves these spawn locations
 * in a hashmap, and gives these locations.
 *
 * @author lelesape
 */
public class RandomSpawnCache {

    /**
     * The only instance for this class.
     */
    private static RandomSpawnCache instance;

    /**
     * A list of safe to spawn locations for the given world in config.
     */
    private List<Location> spawnLocations;
    /**
     * The plugin's main instance.
     */
    final private AreaSpawner plugin;
    /**
     * The runnable that will create the safe locations.
     */
    final private BukkitRunnable runnable = new BukkitRunnable() {
        @Override
        public void run() {
            createSafeLocations();
        }
    };


    /**
     * Creates a new RandomSpawnCache instance, private for helping on applying Singleton pattern.
     * @param plugin The main AreaSpawner plugin instance.
     */
    private RandomSpawnCache(AreaSpawner plugin){
        this.plugin = plugin;
        this.spawnLocations = new ArrayList<>();
        createNewSafeSpawns();
        ConsoleLogger.debug("Cache successfully initialized");
    }



    /**
     * Empties the safe spawn locations list and fills it with new locations (async).
     */
    public void createNewSafeSpawns(){
        runnable.runTaskAsynchronously(plugin);
    }



    /**
     * Gets a random element from the list of safeSpawns.
     * @return A safe spawn ready for a player to spawn or null if no spawns were generated for the given world
     * or no spawn locations were loaded for the given world.
     */
    public Location getSafeSpawn(){
        Location location = spawnLocations.get(new Random().nextInt(spawnLocations.size()));
        ConsoleLogger.debug("&eA location has been used");

        if(Settings.getInstance().isReplaceUsedLocation()) {
            ConsoleLogger.debug("&eGenerating a new location in replacement");

            spawnLocations.remove(location);

            new BukkitRunnable() {
                @Override
                public void run() {
                    replaceLocation();
                }

            }.runTaskAsynchronously(plugin);
        }


        return location.clone().add(0.5,1,0.5);
    }



    /**
     * Replaces a used location for a new one.
     */
    private void replaceLocation(){
        Region allowed = Settings.getInstance().getAllowedRegion();
        Region forbidden = Settings.getInstance().getForbiddenRegion();

        Location loc = allowed.generateNewLocation(forbidden);

        if(loc == null){
            ConsoleLogger.debug("&cFailed to add replacement location after "+Settings.getInstance().getFindSafeLocationAttempts()+" attempts");
        }else {
            ConsoleLogger.debug("&aReplacement location successfully added!");
            spawnLocations.add(loc);
        }
    }


    /**
     * Checks every safe spawn saved is actually safe.
     * If the world is null, every location saved for said world is deleted.
     * If the range of coordinates has changed and the no-spawn region is greater than the spawn region, said
     */
    public void reValidateSpawns(){
        //Fixme: Async?
        for(Location loc : spawnLocations){
            if(!isValidLocation(loc, Settings.getInstance().getForbiddenRegion())){
                spawnLocations.remove(loc);
                ConsoleLogger.debug("&cRemoved a location from the locations list");
            }
        }

    }


    /**
     * Creates as many safe spawn locations as specified in config for each world specified in config and adds them to
     * the world,locations map.
     */
    public void createSafeLocations(){
        Settings settings = Settings.getInstance();

        if(!settings.isCacheEnabled()){
            ConsoleLogger.send("&eWARNING &f- Location cache is disabled. Locations will be calculated on the spot, players may take a while to respawn depending on your other settings.");
            return;
        }

        ConsoleLogger.send("&eCreating safe locations...");

        Region allowed = settings.getAllowedRegion();
        Region forbidden = settings.getForbiddenRegion();
        int amount = settings.getCachedLocationsAmount();

        spawnLocations = new ArrayList<>();


        for (int i = 1; i <= amount; i++) {
            ConsoleLogger.debug("&eAttempting to add location number "+i);

            Location loc = allowed.generateNewLocation(forbidden);

            if(loc == null){
                ConsoleLogger.debug("&cFailed to add location number "+i+" after "+settings.getFindSafeLocationAttempts()+" attempts");
            }else {
                ConsoleLogger.debug("&aLocation number "+i+" successfully added!");
                spawnLocations.add(loc);
            }

            try {
                Thread.sleep(settings.getTimeBetweenLocations());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


        ConsoleLogger.send("&aSuccessfully added "+spawnLocations.size()+" safe spawn locations");

    }


    //TEMPORAL. USED FOR TESTING
    public List<Location> getLocationsInCache(){
        return spawnLocations;
    }



    /**
     * Checks if a location follows a number of steps for considering it "safe" enough for a player to spawn in.
     * @param loc The location to analyze.
     * @param forbidden The "forbidden" spawn region for this location's world.
     * @return true if the location passed every check.
     */
    public boolean isValidLocation(Location loc, Region forbidden){
        //Steps for getting a safe location:
        // 1. y is greater than 0 and lesser than 255.
        // 2. x and z are within the spawn region and outside no-spawn region.
        // 3. there is 2 block air gap above location.
        // 4. block is not in blacklist.
        // 5. block is in whitelist or blocks not in whitelist are safe.
        FileConfiguration config = plugin.getConfigYaml().getAccess();

        if(loc.getY() < 1 || loc.getY() > 255){
            ConsoleLogger.send("&cNo non-air block found.");
            return false;
        }
        if(forbidden.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())){
            ConsoleLogger.debug("&cLocation is in no-spawn region.");
            return false;
        }
        if(!loc.clone().add(0,1,0).getBlock().getType().equals(Material.AIR) || !loc.clone().add(0,2,0).getBlock().getType().equals(Material.AIR)){
            ConsoleLogger.debug("&cNo 2 block high air gap found.");
            return false;
        }
        if(config.getBoolean("config.blocks.blacklist.enabled") && config.getStringList("config.blocks.blacklist.list").contains(loc.getBlock().getType().toString())){
            ConsoleLogger.debug("&cBlock "+loc.getBlock().getType().toString()+" is in blacklist.");
            return false;
        }
        if(!config.getBoolean("config.blocks.whitelist.non-whitelist are safe") && !config.getStringList("config.blocks.whitelist.list").contains(loc.getBlock().getType().toString())){
            ConsoleLogger.debug("&cBlock "+loc.getBlock().getType().toString()+" is not in whitelist.");
            return false;
        }


        return true;
    }

    /**
     * Saves the locations in cache to the cache file.
     */
    public void saveToFile(){
        //TODO: md5 hash?
        FileConfiguration cache = plugin.getCacheYaml().getAccess();

        cache.set("cache", this.spawnLocations);

        plugin.getCacheYaml().save();
    }

    /**
     * Loads every location in the cache file.
     */
    public void loadFromFile(){
        //TODO: md5 hash?
        ConfigurationSection cache = plugin.getCacheYaml().getAccess().getConfigurationSection("cache");

        //If no cache section is found
        if(cache == null) return;

        for (String worldName : cache.getKeys(false)) {
            //TODO: load spawns from cache file, verify them and if valid add them.
        }
    }



    /**
     * Creates an instance of RandomSpawnCache if none found.
     * @param plugin The main class instance.
     */
    public static void createInstance(AreaSpawner plugin){
        if(instance == null){
            instance = new RandomSpawnCache(plugin);
        }
    }

    /**
     * Gets the single instance of this class. {@link RandomSpawnCache#createInstance(AreaSpawner)} should be run first.
     * @return An instance of this RandomSpawnCache and the only one in existence.
     */
    public static RandomSpawnCache getInstance(){
        return instance;
    }



}
