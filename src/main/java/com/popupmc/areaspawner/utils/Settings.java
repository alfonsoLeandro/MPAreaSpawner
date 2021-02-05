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

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.popupmc.areaspawner.AreaSpawner;
import com.popupmc.areaspawner.spawn.Region;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Class containing AreaSpawner's sensible settings, loaded from the config.yml file.
 *
 * @author lelesape
 */
public class Settings {

    /**
     * The only instance for this class.
     */
    static private Settings instance;

    /**
     * AreaSpawner's main class instance.
     */
    final private AreaSpawner plugin;

    //Settings
    private boolean debug;
    private boolean removeUsedLocation;
    private boolean cacheEnabled;
    private boolean saveCacheToFile;
    private boolean topToBottom;
    private boolean checkPastSurface;
    private boolean checkSafetyOnUse;
    private boolean deleteOnUnsafe;
    private boolean replaceRemovedLocation;
    private boolean spawnOnDeath;
    private boolean spawnOnFirstJoin;
    private boolean spawnOnBed;
    private boolean useAutomaticPermission;
    private boolean essentialsHomeOnRespawn;
    private boolean essentialsSetHomeOnFirstJoin;
    private boolean essentialsSetHomeOnTravel;
    private boolean travelEnabled;
    private boolean removePermissionOnTravel;
    private boolean listIsWhitelist;
    private int cachedLocationsAmount;
    private int findSafeLocationAttempts;
    private int airGapAbove;
    private int timeBetweenLocations;
    private int timeBetweenAttempts;
    private int travelCooldown;
    private String prefix;
    private String worldName;
    private String firstJoinHomeName;
    private String travelHomeName;
    private List<String> blockList;
    private World world;
    private Region allowedRegion;
    private Region forbiddenRegion;

    /**
     * Creates a new Settings instance, private for helping on applying Singleton pattern.
     * @param plugin AreaSpawner's main class instance.
     */
    private Settings(AreaSpawner plugin){
        instance = this;
        this.plugin = plugin;
        reloadFields();
    }

    /**
     * Loads/reloads every field contained in this class.
     */
    public void reloadFields(){
        FileConfiguration config = plugin.getConfig();

        this.debug = config.getBoolean("debug");
        this.removeUsedLocation = config.getBoolean("delete location on use");
        this.cacheEnabled = config.getBoolean("enable cache");
        this.saveCacheToFile = config.getBoolean("save cache to file");
        this.topToBottom = config.getBoolean("top to bottom");
        this.checkPastSurface = config.getBoolean("check past surface");
        this.checkSafetyOnUse = config.getBoolean("re-check for safety on use");
        this.deleteOnUnsafe = config.getBoolean("delete location on unsafe");
        this.replaceRemovedLocation = config.getBoolean("replace location on remove");
        this.spawnOnDeath = config.getBoolean("spawn on death");
        this.spawnOnFirstJoin = config.getBoolean("spawn on first join");
        this.spawnOnBed = config.getBoolean("spawn on bed");
        this.useAutomaticPermission = config.getBoolean("use permission");
        boolean essentialsEnabled = Bukkit.getPluginManager().getPlugin("Essentials") != null && Bukkit.getPluginManager().isPluginEnabled("Essentials");
        this.essentialsHomeOnRespawn = config.getBoolean("essentials home teleport") && essentialsEnabled;
        this.essentialsSetHomeOnFirstJoin = config.getBoolean("essentials home on first spawn") && essentialsEnabled;
        this.essentialsSetHomeOnTravel = config.getBoolean("home on travel") && essentialsEnabled;
        this.travelEnabled = config.getBoolean("travel enabled");
        this.removePermissionOnTravel = config.getBoolean("remove permission on travel");
        this.listIsWhitelist = config.getBoolean("list is whitelist");

        this.findSafeLocationAttempts = config.getInt("safe spawn attempts");
        this.cachedLocationsAmount = config.getInt("amount of cached spawns");
        this.airGapAbove = config.getInt("air gap above");
        String timeBLString = config.getString("time between generating locations");
        if(timeBLString != null && timeBLString.length() > 1) {
            this.timeBetweenLocations = TimeUnit.getTicks(Integer.parseInt(timeBLString.substring(0, timeBLString.length() - 1)), timeBLString.charAt(timeBLString.length() - 1));
        }else{
            this.timeBetweenLocations = TimeUnit.getTicks(3, TimeUnit.SECONDS);
        }
        String timeBAString = config.getString("time between location attempts");
        if(timeBAString != null && timeBAString.length() > 1) {
            this.timeBetweenAttempts = TimeUnit.getTicks(Integer.parseInt(timeBAString.substring(0, timeBAString.length() - 1)), timeBAString.charAt(timeBAString.length() - 1));
        }else{
            this.timeBetweenAttempts = TimeUnit.getTicks(5, TimeUnit.TICKS);
        }
        String cooldownString = config.getString("travel cooldown");
        if(cooldownString != null && cooldownString.length() > 1) {
            this.travelCooldown = TimeUnit.getTicks(Integer.parseInt(cooldownString.substring(0, cooldownString.length() - 1)), cooldownString.charAt(cooldownString.length() - 1));
        }else{
            this.travelCooldown = TimeUnit.getTicks(0, TimeUnit.SECONDS);
        }

        this.prefix = config.getString("prefix");
        this.worldName = config.getString("spawn world");
        this.firstJoinHomeName = config.getString("home on first spawn name");
        this.travelHomeName = config.getString("home on travel name");

        this.blockList = config.getStringList("block list");

        this.world = Bukkit.getWorld(worldName);

        defineAllowedRegion();
        defineForbiddenRegion();
    }

    /**
     * Defines the {@link #allowedRegion} field with the criteria given in the config file.
     */
    private void defineAllowedRegion(){
        FileConfiguration config = plugin.getConfig();
        boolean multiverseEnabled = Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null && Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core");

        boolean clampToLimits = config.getBoolean("spawn zone.clamp to limits");

        int xCenter;
        int yCenter;
        int zCenter;

        //Multiverse, essentials and vanilla spawnpoints.
        if(config.getInt("spawn zone.x center") == -1) {
            if(config.getBoolean("spawn zone.default to multiverse") && multiverseEnabled){
                xCenter = JavaPlugin.getPlugin(MultiverseCore.class).getMVWorldManager().getMVWorld(world).getSpawnLocation().getBlockX();
            }else{
                xCenter = world.getSpawnLocation().getBlockX();
            }
        }else{
            xCenter = clampToLimits ? Math.min(29_999_984, config.getInt("spawn zone.x center")) : config.getInt("spawn zone.x center");
        }

        //Multiverse, essentials and vanilla spawnpoints.
        if(config.getInt("spawn zone.y center") == -1) {
            if(config.getBoolean("spawn zone.default to multiverse") && multiverseEnabled){
                yCenter = JavaPlugin.getPlugin(MultiverseCore.class).getMVWorldManager().getMVWorld(world).getSpawnLocation().getBlockY();
            }else{
                yCenter = world.getSpawnLocation().getBlockY();
            }
        }else{
            yCenter = clampToLimits ? Math.min(255, config.getInt("spawn zone.y center")) : config.getInt("spawn zone.y center");
        }

        //Multiverse, essentials and vanilla spawnpoints.
        if(config.getInt("spawn zone.z center") == -1) {
            if(config.getBoolean("spawn zone.default to multiverse") && multiverseEnabled){
                zCenter = JavaPlugin.getPlugin(MultiverseCore.class).getMVWorldManager().getMVWorld(world).getSpawnLocation().getBlockZ();
            }else{
                zCenter = world.getSpawnLocation().getBlockZ();
            }
        }else{
            zCenter = clampToLimits ? Math.min(29_999_984, config.getInt("spawn zone.z center")) : config.getInt("spawn zone.z center");
        }

        int xRange = clampToLimits ? Math.min(29_999_984, config.getInt("spawn zone.x range")) : config.getInt("spawn zone.x range");
        int yRange = clampToLimits ? Math.min(255, config.getInt("spawn zone.y range")) : config.getInt("spawn zone.y range");
        int zRange = clampToLimits ? Math.min(29_999_984, config.getInt("spawn zone.z range")) : config.getInt("spawn zone.z range");

        if(clampToLimits){
            if(xCenter+xRange > 29_999_984 || xCenter-xRange < -29_999_984) xRange = 29_999_984 - xCenter;
            if(yCenter+yRange > 256 || yCenter-yRange < 0) yRange = 128 - yCenter;
            if(zCenter+zRange > 29_999_984 || zCenter-zRange < -29_999_984) zRange = 29_999_984 - zCenter;
        }

        Logger.debug("Region final centers: "+xCenter+" "+yCenter+" "+zCenter);
        Logger.debug("Region final ranges: "+xRange+" "+yRange+" "+zRange);

        this.allowedRegion = Region.newRegionByRanges(xCenter, yCenter, zCenter,
                xRange, yRange, zRange);
    }

    /**
     * Defines the {@link #forbiddenRegion} field with the criteria given in the config file.
     */
    private void defineForbiddenRegion() {
        FileConfiguration config = plugin.getConfig();

        boolean enabled = config.getBoolean("no spawn zone.enabled");

        int xCenter = enabled ? config.getInt("spawn zone.x center"): 0;
        int zCenter = enabled ? config.getInt("spawn zone.z center"): 0;

        int xRange = enabled ? config.getInt("no spawn zone.x range") : 0;
        int zRange = enabled ? config.getInt("no spawn zone.z range") : 0;

        this.forbiddenRegion = Region.newRegionByRanges(xCenter, 0, zCenter,
                xRange, 0, zRange);
    }




    //Fields getters

    public boolean isDebug(){
        return debug;
    }

    public boolean isRemoveUsedLocation(){
        return removeUsedLocation;
    }

    public boolean isCacheEnabled(){
        return cacheEnabled;
    }

    public boolean isSaveCacheToFile(){
        return saveCacheToFile;
    }

    public boolean isTopToBottom(){
        return topToBottom;
    }

    public boolean isNotCheckPastSurface(){
        return !checkPastSurface;
    }

    public boolean isCheckSafetyOnUse(){
        return checkSafetyOnUse;
    }

    public boolean isDeleteOnUnsafe(){
        return deleteOnUnsafe;
    }

    public boolean isReplaceRemovedLocation(){
        return replaceRemovedLocation;
    }

    public boolean isSpawnOnDeath(){
        return spawnOnDeath;
    }

    public boolean isSpawnOnFirstJoin(){
        return spawnOnFirstJoin;
    }

    public boolean isSpawnOnBed(){
        return spawnOnBed;
    }

    public boolean isNotUseAutomaticPermission(){
        return !useAutomaticPermission;
    }

    public boolean isEssentialsHomeOnRespawn(){
        return essentialsHomeOnRespawn;
    }

    public boolean isEssentialsSetHomeOnFirstJoin(){
        return essentialsSetHomeOnFirstJoin;
    }

    public boolean isEssentialsSetHomeOnTravel(){
        return essentialsSetHomeOnTravel;
    }

    public boolean isTravelEnabled(){
        return travelEnabled;
    }

    public boolean isRemovePermissionOnTravel(){
        return removePermissionOnTravel;
    }

    public boolean isListIsWhitelist(){
        return listIsWhitelist;
    }

    public int getFindSafeLocationAttempts(){
        return findSafeLocationAttempts;
    }

    public int getCachedLocationsAmount(){
        return cachedLocationsAmount;
    }

    public int getAirGapAbove(){
        return airGapAbove;
    }

    public int getTimeBetweenLocations(){
        return timeBetweenLocations;
    }

    public int getTimeBetweenLocationAttempts(){
        return timeBetweenAttempts;
    }

    public int getTravelCooldown(){
        return travelCooldown;
    }

    public String getPrefix(){
        return prefix;
    }

    public String getWorldName(){
        return worldName;
    }

    public String getFirstJoinHomeName(){
        return firstJoinHomeName;
    }

    public String getTravelHomeName(){
        return travelHomeName;
    }

    public List<String> getBlockList(){
        return blockList;
    }

    public World getWorld(){
        return world;
    }

    public Region getAllowedRegion(){
        return allowedRegion;
    }

    public Region getForbiddenRegion(){
        return forbiddenRegion;
    }


    /**
     * Creates an instance of TravelCooldownManager if none found.
     * @param plugin AreaSpawner's main class instance.
     */
    public static void createInstance(AreaSpawner plugin){
        instance = new Settings(plugin);
    }

    /**
     * Gets the single instance of this class. {@link #createInstance(AreaSpawner)} should be run first.
     * @return An instance of this Settings and the only one in existence.
     */
    public static Settings getInstance(){
        return instance;
    }




}
