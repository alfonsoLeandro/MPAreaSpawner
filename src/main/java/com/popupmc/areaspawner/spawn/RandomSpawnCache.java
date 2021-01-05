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
import org.bukkit.block.BlockFace;
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
    final private BukkitRunnable createSafeLocationsAsync = new BukkitRunnable() {
        @Override
        public void run() {
            createSafeLocations();
        }
    };
    /**
     * The runnable that will create a single location.
     */
    final private BukkitRunnable createNewSingleLocationAsync = new BukkitRunnable() {
        @Override
        public void run() {
            replaceLocation();
        }
    };


    /**
     * Creates a new RandomSpawnCache instance, private for helping on applying Singleton pattern.
     * @param plugin The main AreaSpawner plugin instance.
     */
    private RandomSpawnCache(AreaSpawner plugin){
        this.plugin = plugin;
        this.spawnLocations = new ArrayList<>();
        if(Settings.getInstance().isCacheEnabled()) {
            new BukkitRunnable() {public void run() {loadFromFile();}}.runTaskAsynchronously(plugin);
            createSafeSpawns(false);
            ConsoleLogger.send("Cache successfully initialized");
        }else{
            ConsoleLogger.send("&eWARNING &f- Location cache is disabled. Locations will be calculated on the spot, players may take a while to respawn depending on your other settings.");
        }
    }



    /**
     * Empties the safe spawn locations list and fills it with new locations (async).
     */
    public void createSafeSpawns(boolean clear){
        if(clear) spawnLocations = new ArrayList<>();
        createSafeLocationsAsync.runTaskAsynchronously(plugin);
    }



    /**
     * Gets a random element from the list of safeSpawns.
     * @return A safe spawn ready for a player to spawn or null if no spawns were generated for the given world
     * or no spawn locations were loaded for the given world.
     */
    public Location getSafeSpawn(){
        Random r = new Random();
        Settings settings = Settings.getInstance();

        Location location = spawnLocations.get(r.nextInt(spawnLocations.size()));

        if(settings.isCheckSafetyOnUse()) {
            while (!isValidLocation(location, settings.getForbiddenRegion())) {
                if(settings.isDeleteOnUnsafe()) {
                    ConsoleLogger.debug("&cA previously considered safe location is no longer safe, generating a new one in replacement.");
                    spawnLocations.remove(location);
                    createNewSingleLocationAsync.runTaskAsynchronously(plugin);
                }
                location = spawnLocations.get(r.nextInt(spawnLocations.size()));
            }
        }

        ConsoleLogger.debug("&eA location has been used");

        if(settings.isReplaceUsedLocation()) {
            ConsoleLogger.debug("&eGenerating a new location in replacement");

            spawnLocations.remove(location);
            createNewSingleLocationAsync.runTaskAsynchronously(plugin);
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
        int i = spawnLocations.size()+1;
        int added = 0;


        for (;i <= amount; i++) {
            ConsoleLogger.debug("&eAttempting to add location number "+i);

            Location loc = allowed.generateNewLocation(forbidden);

            if(loc == null){
                ConsoleLogger.debug("&cFailed to add location number "+i+" after "+settings.getFindSafeLocationAttempts()+" attempts");
            }else {
                ConsoleLogger.debug("&aLocation number "+i+" successfully added!");
                spawnLocations.add(loc);
                added++;
            }

            try {
                Thread.sleep(settings.getTimeBetweenLocations());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


        ConsoleLogger.send("&aSuccessfully added "+added+" new safe spawn locations");

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
        // 3. there is a block air gap above location.
        // 4. block is not in blacklist.
        // 5. block is in whitelist or blocks not in whitelist are safe.
        Settings settings = Settings.getInstance();
        String blockType = loc.getBlock().getType().toString();

        if(loc.getY() < 1 || loc.getY() > 255){
            ConsoleLogger.send("&cNo non-air block found.");
            return false;
        }
        if(forbidden.contains2D(loc.getBlockX(), loc.getBlockZ())){
            ConsoleLogger.debug("&cLocation is in no-spawn region.");
            return false;
        }
        if(hasAirGap(loc)){
            ConsoleLogger.debug("&cThe air gap was not tall enough, or there were none at all.");
            return false;
        }
        if(settings.getBlockBlackList().contains(blockType)){
            ConsoleLogger.debug("&cBlock "+blockType+" is in blacklist.");
            return false;
        }
        if(!settings.getBlockWhiteList().contains(blockType) && !settings.isNonWhiteListSafe()){
            ConsoleLogger.debug("&cBlock "+blockType+" is not in whitelist and non-whitelist are non-safe.");
            return false;
        }


        return true;
    }

    private boolean hasAirGap(Location loc){
        int airGap = Settings.getInstance().getAirGapAbove();

        for (int i = 1; i <= airGap ; i++) {
            if(!loc.getBlock().getRelative(BlockFace.UP, i).getType().equals(Material.AIR)){
                return false;
            }
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
        FileConfiguration cache = plugin.getCacheYaml().getAccess();
        ConsoleLogger.debug("&fLoading locations from cache file...");

        //If no cache section is found
        if(!cache.contains("cache")){
            ConsoleLogger.debug("&cNo locations were found. Creating new ones instead.");
            return;
        }

        List<Location> locations = (List<Location>) cache.getList("cache");

        ConsoleLogger.debug("&aFound &f"+locations.size()+"&a locations to load.");

        for (Location loc : locations){
            if(isValidLocation(loc, Settings.getInstance().getForbiddenRegion())){
                spawnLocations.add(loc);
                ConsoleLogger.debug("&aAdded a location from the cache file.");
            }else{
                ConsoleLogger.debug("&cA location in the cache file was not safe and therefore not added to the spawn list.");
            }
        }

        ConsoleLogger.debug("&fFinished loading locations from cache file.");
        ConsoleLogger.debug("&f"+spawnLocations.size()+"/"+locations.size()+" safe locations were loaded from the cache file");

        cache.set("cache", null);
        plugin.getCacheYaml().save();
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
