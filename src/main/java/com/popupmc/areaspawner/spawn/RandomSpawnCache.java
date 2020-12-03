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
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
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
     * A hashmap containing a list of safe to spawn locations for each world in config.
     */
    private final HashMap<String, List<Location>> spawnLocations;
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
        this.spawnLocations = new HashMap<>();
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


    /**
     * Replaces a used location for a new one.
     * @param toReplace The location to remove from the list of locations.
     */
    private void replaceLocation(Location toReplace){
        FileConfiguration config = plugin.getConfig();

        String worldName = toReplace.getWorld().getName();

        Region allowed = new Region(config.getInt("config.spawn zone."+worldName+".min x"),
                config.getInt("config.spawn zone."+worldName+".max x"),
                config.getInt("config.spawn zone."+worldName+".min z"),
                config.getInt("config.spawn zone."+worldName+".max z"));

        Region forbidden;

        if(config.getBoolean("config.no spawn zone."+worldName+".enabled")) {
            forbidden = new Region(config.getInt("config.no spawn zone." + worldName + ".min x"),
                    config.getInt("config.no spawn zone." + worldName + ".max x"),
                    config.getInt("config.no spawn zone." + worldName + ".min z"),
                    config.getInt("config.no spawn zone." + worldName + ".max z"));
        }else{
            forbidden = new Region(0,0,0,0);
        }

        Location loc = generateNewSafeLocation(toReplace.getWorld(), forbidden, allowed);

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

        for(String worldName : spawnLocations.keySet()){
            List<Location> locations = new ArrayList<>(spawnLocations.get(worldName));
            World world = Bukkit.getWorld(worldName);

            if(world == null){
                debug("&cWorld "+worldName+" is null.");
                debug("&cRemoving every spawn location for this world");
                spawnLocations.remove(worldName);
                continue;
            }


            Region allowed = new Region(config.getInt("config.spawn zone."+worldName+".min x"),
                    config.getInt("config.spawn zone."+worldName+".max x"),
                    config.getInt("config.spawn zone."+worldName+".min z"),
                    config.getInt("config.spawn zone."+worldName+".max z"));

            Region forbidden;

            if(config.getBoolean("config.no spawn zone."+worldName+".enabled")) {
                forbidden = new Region(config.getInt("config.no spawn zone." + worldName + ".min x"),
                        config.getInt("config.no spawn zone." + worldName + ".max x"),
                        config.getInt("config.no spawn zone." + worldName + ".min z"),
                        config.getInt("config.no spawn zone." + worldName + ".max z"));
            }else{
                forbidden = new Region(0,0,0,0);
            }

            if(forbidden.contains(allowed)){
                debug("&cThe no-spawn region is greater than the spawn region for world "+worldName);
                debug("&cPlease correct this. Every location for this world will be removed.");
                spawnLocations.remove(worldName);
                continue;
            }



            for(Location loc : locations){
                if(!isValidLocation(loc, forbidden)){
                    spawnLocations.get(worldName).remove(loc);
                    debug("&cRemoved a location from the locations list for "+worldName);
                }
            }
        }
    }


    /**
     * Creates as many safe spawn locations as specified in config for each world specified in config and adds them to
     * the world,locations map.
     */
    public void createSafeLocations(){
        FileConfiguration config = plugin.getConfigYaml().getAccess();

        send("&eCreating safe locations...");

        for(String worldName : config.getConfigurationSection("config.spawn zone").getKeys(false)){
            World world = Bukkit.getWorld(worldName);

            if(world == null){
                debug("&cWorld "+worldName+" is null.");
                continue;
            }

            Region allowed = new Region(config.getInt("config.spawn zone."+worldName+".min x"),
                    config.getInt("config.spawn zone."+worldName+".max x"),
                    config.getInt("config.spawn zone."+worldName+".min z"),
                    config.getInt("config.spawn zone."+worldName+".max z"));

            Region forbidden;

            if(config.getBoolean("config.no spawn zone."+worldName+".enabled")) {
                forbidden = new Region(config.getInt("config.no spawn zone." + worldName + ".min x"),
                        config.getInt("config.no spawn zone." + worldName + ".max x"),
                        config.getInt("config.no spawn zone." + worldName + ".min z"),
                        config.getInt("config.no spawn zone." + worldName + ".max z"));
            }else{
                forbidden = new Region(0,0,0,0);
            }

            if(forbidden.contains(allowed)){
                debug("&cThe no-spawn region is greater than the spawn region for world "+worldName);
                debug("&cPlease correct this. spawn calculation for this world aborted.");
                continue;
            }

            spawnLocations.put(worldName, new ArrayList<>());
            List<Location> locations = spawnLocations.get(worldName);


            for (int i = 1; i <= config.getInt("config.cache.spawns"); i++) {
                debug("&eAttempting to add location number "+i+" for world "+worldName);

                Location loc = generateNewSafeLocation(world, forbidden, allowed);

                if(loc == null){
                    debug("&cFailed to add location number "+i+" after "+config.getInt("config.cache.safe spawn attempts")+" attempts");
                }else {
                    debug("&aLocation number "+i+" successfully added!");
                    locations.add(loc);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }


            send("&aSuccessfully added "+locations.size()+" safe spawn locations for world "+worldName);
        }
    }

    /**
     * Generates a new safe to spawn location with the given parameters.
     * @param world The world to generate the location for.
     * @param forbidden The region where spawn locations are forbidden. This region should be inside the allowed region.
     * @param allowed The region where spawn locations are allowed.
     * @return A safe to spawn location with the given parameters or null if failed to generate one after 25 attempts.
     */
    private Location generateNewSafeLocation(World world, Region forbidden, Region allowed) {
        Location making = new Location(world, 0, -10, 0);
        Random r = new Random();
        int attempts = plugin.getConfig().getInt("config.cache.safe spawn attempts");

        //Make x amount of attempts before giving up and calculating the next one
        for (int i = 1; i <= attempts ; i++) {
            debug("&eAttempt number " + i + " to generate location.");

            making.setX(r.nextInt(allowed.getMaxX() - allowed.getMinX()) + allowed.getMinX());
            making.setZ(r.nextInt(allowed.getMaxZ() - allowed.getMinZ()) + allowed.getMinZ());
            making.setY(getHighestY(making));

            //FixME: REMOVE
            Bukkit.broadcastMessage("Block: " + making.getBlock().toString());


            if(isValidLocation(making, forbidden)) {
                debug("&aSafe valid location achieved!");
                return making;
            }

            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        return null;
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
            send("&cNo non-air block found.");
            return false;
        }
        if(forbidden.contains(loc.getBlockX(), loc.getBlockZ())){
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
