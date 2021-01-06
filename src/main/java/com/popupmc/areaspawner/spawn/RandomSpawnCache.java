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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
     * The runnable that will create a single location.
     */
    final private BukkitRunnable createNewSingleLocationAsync = new BukkitRunnable() {
        @Override
        public void run() {
            replaceLocation();
        }
    };
    private BukkitTask cacheGeneratorTask;


    /**
     * Creates a new RandomSpawnCache instance, private for helping on applying Singleton pattern.
     * @param plugin The main AreaSpawner plugin instance.
     */
    private RandomSpawnCache(AreaSpawner plugin){
        this.plugin = plugin;
        this.spawnLocations = new ArrayList<>();
        if(Settings.getInstance().isCacheEnabled()) {
            loadFromFile();
            createSafeSpawns(false);
            ConsoleLogger.send("Cache successfully initialized");
        }else{
            ConsoleLogger.send("&eWARNING &f- Location cache is disabled. Locations will be calculated on the spot, players may take a while to respawn depending on your other settings.");
        }
    }


    /**
     * Fills the list of safe spawn locations with new ones.
     * @param clear Whether to clear the current list of safe spawn locations.
     */
    public void createSafeSpawns(boolean clear){
        if(clear) spawnLocations = new ArrayList<>();
        createSafeLocations();
    }

    /**
     * Stops the current cache task from creating new safe spawn locations.
     * @return true if the cache was successfully stopped.
     */
    public boolean stopCache(){
        if(cacheGeneratorTask == null || cacheGeneratorTask.isCancelled()) return false;
        cacheGeneratorTask.cancel();
        return true;
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
            while (!Region.isValidLocation(location, settings.getForbiddenRegion(), settings.getAllowedRegion())) {
                if(settings.isDeleteOnUnsafe()) {
                    ConsoleLogger.debug("&cA previously considered safe location is no longer safe, generating a new one in replacement.");
                    removeLocation(location);
                }
                location = spawnLocations.get(r.nextInt(spawnLocations.size()));
            }
        }

        ConsoleLogger.debug("&eA location has been used");

        if(settings.isRemoveUsedLocation()) {
            ConsoleLogger.debug("&eRemoving the used location.");
            removeLocation(location);
        }


        return location.clone().add(0.5,1,0.5);
    }



    /**
     * Replaces a used location for a new one.
     */
    private void replaceLocation(){
        Settings settings = Settings.getInstance();
        Region allowed = settings.getAllowedRegion();
        Region forbidden = settings.getForbiddenRegion();

        Location loc = allowed.generateNewLocation(forbidden);

        if(loc == null){
            ConsoleLogger.debug("&cFailed to add replacement location after "+settings.getFindSafeLocationAttempts()+" attempts");
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
        Settings settings = Settings.getInstance();
        for(Location loc : spawnLocations){
            if(!Region.isValidLocation(loc, settings.getForbiddenRegion(), settings.getAllowedRegion())){
                ConsoleLogger.debug("&cA location has been detected as no longer safe.");
                if(settings.isDeleteOnUnsafe()) {
                    removeLocation(loc);
                }
            }
        }

    }

    /**
     * Removes a location from the locations list and replaces it if
     * "replace location on remove" is set to true in config.
     * @param loc The location to remove from the list.
     */
    private void removeLocation(Location loc){
        Settings settings = Settings.getInstance();

        spawnLocations.remove(loc);
        ConsoleLogger.debug("&aLocation successfully removed from the locations list");
        if(settings.isReplaceRemovedLocation()){
            ConsoleLogger.debug("&eCreating a new location in replacement.");
            createNewSingleLocationAsync.runTaskAsynchronously(plugin);
        }

    }


    /**
     * Creates as many safe spawn locations as specified in config and adds them to
     * the list of locations.
     */
    public void createSafeLocations(){
        Settings settings = Settings.getInstance();

        if(!settings.isCacheEnabled()){
            ConsoleLogger.send("&eWARNING &f- Location cache is disabled. Locations will be calculated on the spot, players may take a while to respawn depending on your other settings.");
            return;
        }

        ConsoleLogger.send("&eCreating safe locations...");

        final Region allowed = settings.getAllowedRegion();
        final Region forbidden = settings.getForbiddenRegion();
        final int amountOfLocationsToAdd = settings.getCachedLocationsAmount();
        final int[] locationNumber = {spawnLocations.size() + 1};
        final int[] addedLocations = {0, 0};


        cacheGeneratorTask = new BukkitRunnable(){

            @Override
            public void run(){
                if(locationNumber[0] > amountOfLocationsToAdd){
                    showAddedLocations(addedLocations[0], addedLocations[1]);
                    cancel();
                    return;
                }

                ConsoleLogger.debug("&eAttempting to add location number "+ locationNumber[0]);

                Location loc = allowed.generateNewLocation(forbidden);

                if(loc == null){
                    ConsoleLogger.debug("&cFailed to add location number "+ locationNumber[0] +" after "+settings.getFindSafeLocationAttempts()+" attempts");
                    addedLocations[1]++;
                }else {
                    ConsoleLogger.debug("&aLocation number "+ locationNumber[0] +" successfully added!");
                    spawnLocations.add(loc);
                    addedLocations[0]++;
                }
                locationNumber[0]++;

            }

        }.runTaskTimerAsynchronously(plugin, 5, settings.getTimeBetweenLocations());

//      Previous implementation.
//        for (;i <= amount; i++) {
//            ConsoleLogger.debug("&eAttempting to add location number "+i);
//
//            Location loc = allowed.generateNewLocation(forbidden);
//
//            if(loc == null){
//                ConsoleLogger.debug("&cFailed to add location number "+i+" after "+settings.getFindSafeLocationAttempts()+" attempts");
//            }else {
//                ConsoleLogger.debug("&aLocation number "+i+" successfully added!");
//                spawnLocations.add(loc);
//                added++;
//            }
//
//            try {
//                Thread.sleep(settings.getTimeBetweenLocations());
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * Shows in console how many locations have been added and how many of them failed to be added.
     * @param succeeded The amount of locations that were successfully added to the list.
     * @param failed The amount of locations that, for some reason or another, were not able to be added.
     */
    private void showAddedLocations(int succeeded, int failed){
        if(succeeded > 0) {
            ConsoleLogger.send("&aSuccessfully added " + succeeded + " new safe spawn locations");
        }else{
            ConsoleLogger.send("&fNo new locations were added.");
        }
        if(failed > 0){
            ConsoleLogger.send("&cFailed to add "+ failed +" safe spawn locations");
        }
    }

    /**
     * Gets the amount of locations stored in cache at the moment.
     * @return The amount of safe to spawn locations stored in cache.
     */
    public int getLocationsInCache(){
        return spawnLocations.size();
    }


    /**
     * Saves the locations in cache to the cache file.
     */
    public void saveToFile(){
        //TODO: md5 hash?
        Settings settings = Settings.getInstance();

        if(settings.isSaveCacheToFile()) {
            FileConfiguration cache = plugin.getCacheYaml().getAccess();

            cache.set("cache", this.spawnLocations);

            plugin.getCacheYaml().save();

            ConsoleLogger.send("&aCache saved to cache file successfully!");
        }
    }

    /**
     * Loads every location in the cache file.
     */
    public void loadFromFile(){
        //TODO: md5 hash?
        Settings settings = Settings.getInstance();

        if(settings.isSaveCacheToFile()) {
            FileConfiguration cache = plugin.getCacheYaml().getAccess();
            ConsoleLogger.debug("&fLoading locations from cache file...");

            //If no cache section is found
            if(!cache.contains("cache")) {
                ConsoleLogger.debug("&cNo locations were found. Creating new ones instead.");
                return;
            }

            List<Location> locations = (List<Location>) cache.getList("cache");

            ConsoleLogger.debug("&aFound &f" + locations.size() + "&a locations to load.");

            for (Location loc : locations) {
                if(Region.isValidLocation(loc, settings.getForbiddenRegion(), settings.getAllowedRegion())) {
                    spawnLocations.add(loc);
                    ConsoleLogger.debug("&aAdded a location from the cache file.");
                } else {
                    ConsoleLogger.debug("&cA location in the cache file was not safe and therefore not added to the spawn list.");
                }
            }

            ConsoleLogger.debug("&fFinished loading locations from cache file.");
            ConsoleLogger.debug("&f" + spawnLocations.size() + "/" + locations.size() + " safe locations were loaded from the cache file");

            cache.set("cache", null);
            plugin.getCacheYaml().save();
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
