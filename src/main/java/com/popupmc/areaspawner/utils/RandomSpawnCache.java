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
package com.popupmc.areaspawner.utils;

import com.popupmc.areaspawner.AreaSpawner;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class RandomSpawnCache {

    private static RandomSpawnCache instance;
    private HashMap<String, List<Location>> spawnLocations;
    final private AreaSpawner plugin;
    final private BukkitRunnable runnable;

    /**
     * Creates a new RandomSpawnCache instance, private for helping on applying Singleton pattern.
     * @param plugin The main AreaSpawner plugin instance.
     */
    private RandomSpawnCache(AreaSpawner plugin){
        this.plugin = plugin;
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                spawnLocations = createSafeLocations();
            }
        };
        createNewSafeSpawns();
        debug("Cache successfully initialized");
    }

    /**
     * Sends a message to the console.
     * @param msg The message to be sent.
     */
    private void send(String msg){
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("config.prefix")+" "+msg));
    }

    /**
     * Sends a debug message to the console only if the debug mode is enabled.
     * @param msg The message to send.
     */
    private void debug(String msg){
        if(plugin.getConfig().getBoolean("config.debug")) send(msg);
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
    public Location getSafeSpawn(String worldName){
        if(spawnLocations.containsKey(worldName)){
            List<Location> locations = spawnLocations.get(worldName);
            if(locations != null && locations.size() > 0){
                Location toGive = locations.get(new Random().nextInt(spawnLocations.get(worldName).size()));

                locations.remove(toGive);
                debug("&eA location has been used. Generating a new one in replacement.");

                new BukkitRunnable() {
                    @Override
                    public void run(){
                        replaceLocation(toGive);
                    }

                }.runTaskAsynchronously(plugin);


                return toGive.clone().add(0.5,1,0.5);
            }
        }
        return null;
    }

    private void replaceLocation(Location toReplace){
        FileConfiguration config = plugin.getConfig();
        String worldName = toReplace.getWorld().getName();
        int[] xRange = new int[2];
        int[] zRange = new int[2];
        int[] fXRange = new int[2];
        int[] fZRange = new int[2];


        xRange[0] = config.getInt("config.random spawn."+worldName+".min x");
        xRange[1] = config.getInt("config.random spawn."+worldName+".max x");

        zRange[0] = config.getInt("config.random spawn."+worldName+".min z");
        zRange[1] = config.getInt("config.random spawn."+worldName+".max x");

        if(config.getBoolean("config.no spawn."+worldName+".enabled")){
            fXRange[0] = config.getInt("config.no spawn."+worldName+".min x");
            fXRange[1] = config.getInt("config.no spawn."+worldName+".max x");

            fZRange[0] = config.getInt("config.no spawn."+worldName+".min z");
            fZRange[1] = config.getInt("config.no spawn."+worldName+".max z");
        }

        Location loc = generateNewSafeLocation(toReplace.getWorld(), fXRange, fZRange, xRange, zRange);

        if(loc == null){
            debug("&cFailed to add replacement location after 25 attempts");
        }else {
            debug("&aReplacement location successfully added!");
            spawnLocations.get(worldName).add(loc);
        }
    }


    /**
     * Checks every safe spawn saved is actually safe.
     * If the world is null, every location saved for said world is deleted.
     * If the range of coordinates has changed and the no-spawn region is greater than the spawn region, said
     */
    public void reValidateSpawns(){
        FileConfiguration config = plugin.getConfig();
        int[] xRange = new int[2];
        int[] zRange = new int[2];
        int[] fXRange = new int[2];
        int[] fZRange = new int[2];

        for(String worldName : spawnLocations.keySet()){
            List<Location> locations = new ArrayList<>(spawnLocations.get(worldName));
            World world = Bukkit.getWorld(worldName);

            if(world == null){
                debug("&cWorld "+worldName+" is null.");
                debug("&cRemoving every spawn location for this world");
                spawnLocations.remove(worldName);
                continue;
            }

            xRange[0] = config.getInt("config.random spawn."+worldName+".min x");
            xRange[1] = config.getInt("config.random spawn."+worldName+".max x");

            zRange[0] = config.getInt("config.random spawn."+worldName+".min z");
            zRange[1] = config.getInt("config.random spawn."+worldName+".max x");

            if(config.getBoolean("config.no spawn."+worldName+".enabled")) {
                fXRange[0] = config.getInt("config.no spawn." + worldName + ".min x");
                fXRange[1] = config.getInt("config.no spawn." + worldName + ".max x");

                fZRange[0] = config.getInt("config.no spawn." + worldName + ".min z");
                fZRange[1] = config.getInt("config.no spawn." + worldName + ".max z");

                if(isInRegion(fXRange[0], fZRange[0], fXRange[1], fZRange[1], xRange[0], xRange[1]) &&
                        isInRegion(fXRange[0], fZRange[0], fXRange[1], fZRange[1], zRange[0], zRange[1])){
                    debug("&cThe no-spawn region is greater than the spawn region for world "+worldName);
                    debug("&cPlease correct this. Every location for this world will be removed.");
                    spawnLocations.remove(worldName);
                    continue;
                }
            }


            for(Location loc : locations){
                if(!isValidLocation(loc, fXRange, fZRange)){
                    spawnLocations.get(worldName).remove(loc);
                    debug("&cRemoved a location from the locations list for "+worldName);
                }
            }
        }
    }


    public boolean isValidLocation(Location loc, int[] fRegionXValues, int[] fRegionZValues){
        //TODO
        //Steps for getting a safe location:
        // 1. y is greater than 0 and lesser than 255.
        // 2. x and z are within the spawn region and outside no-spawn region.
        // 3. there is 2 block air gap above location.
        // 4. block is not in blacklist.
        // 5. block is in whitelist or blocks not in whitelist are safe.
        FileConfiguration config = plugin.getConfigYaml().getAccess();

        if(loc.getY() < 1 || loc.getY() > 255){
            send("&cNo non-air block found.");
            return false;
        }
        if(isInRegion(fRegionXValues[0], fRegionZValues[0], fRegionXValues[1], fRegionZValues[1], loc.getX(), loc.getZ())){
            debug("&cLocation is in no-spawn region.");
            return false;
        }
        if(!loc.clone().add(0,1,0).getBlock().getType().equals(Material.AIR) || !loc.clone().add(0,2,0).getBlock().getType().equals(Material.AIR)){
            debug("&cNo 2 block high air gap found.");
            return false;
        }
        if(config.getBoolean("config.blocks.blacklist.enabled") && config.getStringList("config.blocks.blacklist.list").contains(loc.getBlock().getType().toString())){
            debug("&cBlock "+loc.getBlock().getType().toString()+" is in blacklist.");
            return false;
        }
        if(!config.getBoolean("config.blocks.whitelist.non-whitelist are safe") && !config.getStringList("config.blocks.whitelist.list").contains(loc.getBlock().getType().toString())){
            debug("&cBlock "+loc.getBlock().getType().toString()+" is not in whitelist.");
            return false;
        }


        return true;
    }

    /**
     * Creates as many safe spawn locations as specified in config for each world specified in config.
     * @return A map containing a list of safe spawns for every world.
     */
    public HashMap<String, List<Location>> createSafeLocations(){
        debug("&eCreating safe locations...");

        FileConfiguration config = plugin.getConfigYaml().getAccess();
        HashMap<String, List<Location>> locationsPerWorld = new HashMap<>();

        for(String worldName : config.getConfigurationSection("config.random spawn").getKeys(false)){
            List<Location> locations = new ArrayList<>();
            World world = Bukkit.getWorld(worldName);
            int[] xRange = new int[2];
            int[] zRange = new int[2];
            int[] fXRange = new int[2];
            int[] fZRange = new int[2];

            if(world == null){
                debug("&cWorld "+worldName+" is null.");
                continue;
            }


            xRange[0] = config.getInt("config.random spawn."+worldName+".min x");
            xRange[1] = config.getInt("config.random spawn."+worldName+".max x");

            zRange[0] = config.getInt("config.random spawn."+worldName+".min z");
            zRange[1] = config.getInt("config.random spawn."+worldName+".max x");

            if(config.getBoolean("config.no spawn."+worldName+".enabled")){
                fXRange[0] = config.getInt("config.no spawn."+worldName+".min x");
                fXRange[1] = config.getInt("config.no spawn."+worldName+".max x");

                fZRange[0] = config.getInt("config.no spawn."+worldName+".min z");
                fZRange[1] = config.getInt("config.no spawn."+worldName+".max z");

                if(isInRegion(fXRange[0], fZRange[0], fXRange[1], fZRange[1], xRange[0], xRange[1]) &&
                        isInRegion(fXRange[0], fZRange[0], fXRange[1], fZRange[1], zRange[0], zRange[1])){
                    debug("&cThe no-spawn region is greater than the spawn region for world "+worldName);
                    debug("&cPlease correct this. spawn calculation for this world aborted.");
                    continue;
                }

            }


            for (int i = 1; i <= config.getInt("config.cache.spawns"); i++) {
                debug("&eAttempting to add location number "+i+" for world "+worldName);

                Location loc = generateNewSafeLocation(world, fXRange, fZRange, xRange, zRange);

                if(loc == null){
                    debug("&cFailed to add location number "+i+" after 25 attempts");
                }else {
                    debug("&aLocation number "+i+" successfully added!");
                    locations.add(loc);
                }
            }


            debug("&aSuccessfully added "+locations.size()+" safe spawn locations for world "+worldName);
            locationsPerWorld.put(worldName, locations);
        }
        return locationsPerWorld;
    }

    /**
     * Generates a new safe to spawn location with the given parameters.
     * @param world The world to generate the location for.
     * @param fRegionXValues The "no spawn" min x and max x values.
     * @param fRegionZValues The "no spawn" min z and max z values.
     * @param regionXValues The allowed to spawn area min x and max x values.
     * @param regionZValues The allowed to spawn area min z and max z values.
     * @return A safe to spawn location with the given parameters or null if failed to generate one after 25 attempts.
     */
    private Location generateNewSafeLocation(World world, int[] fRegionXValues, int[] fRegionZValues, int[] regionXValues, int[] regionZValues){
        //TODO
        Location making = new Location(world, 0, -10, 0);
        Random r = new Random();

        //Make 25 attempts
        for (int i = 0; i < 25; i++) {
            debug("&eAttempt number "+i+" to generate location.");

            making.setX(r.nextInt(regionXValues[1] - regionXValues[0]) + regionXValues[0]);
            making.setZ(r.nextInt(regionZValues[1] - regionZValues[0]) + regionZValues[0]);
            making.setY(getHighestY(making));

            //FixME: REMOVE
            Bukkit.broadcastMessage("Block: "+making.getBlock().toString());


            if(isValidLocation(making, fRegionXValues, fRegionZValues)){
                debug("&aSafe valid location achieved!");
                return making;
            }
        }


        return null;
    }

    /**
     * Checks if a point (x,z) is within a given region.
     * @param regionMinx The minimum x value for the region.
     * @param regionMinZ The minimum z value for the region.
     * @param regionMaxX The maximum x value for the region.
     * @param regionMaxZ The maximum z value for the region.
     * @param pointX The point's x value.
     * @param pointZ The point's y value.
     * @return true if the point is within the given region.
     */
    private boolean isInRegion(int regionMinx, int regionMinZ, int regionMaxX, int regionMaxZ, double pointX, double pointZ){
        return (pointX <= regionMaxX && pointX >= regionMinx) && (pointZ <= regionMaxZ && pointZ >= regionMinZ);
    }


    //TEMPORAL. USED FOR TESTING
    public HashMap<String, List<Location>> getLocationsInCache(){
        return spawnLocations;
    }

    /**
     * Gets the highest Y value for a given location (using x and z)
     * @param loc The world, x and z values to look for.
     * @return The y value for the first non-air block or -10 if none found.
     */
    private int getHighestY(Location loc){
        Location newLoc = loc.clone();
        for (int i = 255; i > 0; i--) {
            newLoc.setY(i);
            if(!newLoc.getBlock().getType().equals(Material.AIR)) return i;
        }
        return -10;
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
