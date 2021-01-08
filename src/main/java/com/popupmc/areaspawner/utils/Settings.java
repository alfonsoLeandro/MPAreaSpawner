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
import com.popupmc.areaspawner.spawn.Region;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Settings {

    static private Settings instance;

    final private AreaSpawner plugin;

    private boolean debug;
    private boolean removeUsedLocation;
    private boolean cacheEnabled;
    private boolean saveCacheToFile;
    private boolean topToBottom;
    private boolean nonWhiteListSafe;
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
    private int cachedLocationsAmount;
    private int findSafeLocationAttempts;
    private int airGapAbove;
    private int timeBetweenLocations;
    private String prefix;
    private String worldName;
    private String firstJoinHomeName;
    private List<String> blockBlackList;
    private List<String> blockWhiteList;
    private World world;
    private Region allowedRegion;
    private Region forbiddenRegion;


    private Settings(AreaSpawner plugin){
        instance = this;
        this.plugin = plugin;
        reloadFields();
    }

    public static void createInstance(AreaSpawner plugin){
        instance = new Settings(plugin);
    }

    public static Settings getInstance(){
        return instance;
    }



    public void reloadFields(){
        FileConfiguration config = plugin.getConfig();

        this.debug = config.getBoolean("debug");
        this.removeUsedLocation = config.getBoolean("delete location on use");
        this.cacheEnabled = config.getBoolean("enable cache");
        this.saveCacheToFile = config.getBoolean("save cache to file");
        this.topToBottom = config.getBoolean("top to bottom");
        this.nonWhiteListSafe = config.getBoolean("non-whitelist are safe");
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

        this.findSafeLocationAttempts = config.getInt("safe spawn attempts");
        this.cachedLocationsAmount = config.getInt("amount of cached spawns");
        this.airGapAbove = config.getInt("air gap above");
        String timeString = config.getString("time between generating locations");
        if(timeString != null && timeString.length() > 1) {
            this.timeBetweenLocations = Time.getTicks(Integer.parseInt(timeString.substring(0, timeString.length() - 1)), TimeUnit.getByAlias(timeString.charAt(timeString.length() - 1)));
        }else{
            this.timeBetweenLocations = Time.getTicks(3, TimeUnit.SECONDS);
        }

        this.prefix = config.getString("prefix");
        this.worldName = config.getString("spawn world");
        this.firstJoinHomeName = config.getString("home on first spawn name", "home");

        this.blockBlackList = config.getStringList("block blacklist");
        this.blockWhiteList = config.getStringList("block whitelist");

        this.world = Bukkit.getWorld(worldName);

        defineAllowedRegion();
        defineForbiddenRegion();
    }

    private void defineAllowedRegion(){
        FileConfiguration config = plugin.getConfig();

        boolean clampToLimits = config.getBoolean("spawn zone.clamp to limits");

        int xCenter;
        int yCenter;
        int zCenter;

        //Multiverse, essentials and vanilla spawnpoints.
        if(config.getInt("spawn zone.x center") == -1) {
            if(config.getBoolean("spawn zone.default to multiverse")){
                //TODO: Multiverse integration.
                xCenter = 0;
            }else{
                xCenter = world.getSpawnLocation().getBlockX();
            }
        }else{
            xCenter = clampToLimits ? Math.min(29_999_984, config.getInt("spawn zone.x center")) : config.getInt("spawn zone.x center");
        }

        //Multiverse, essentials and vanilla spawnpoints.
        if(config.getInt("spawn zone.y center") == -1) {
            if(config.getBoolean("spawn zone.default to multiverse")){
                //TODO: Multiverse integration.
                yCenter = 0;
            }else{
                yCenter = world.getSpawnLocation().getBlockY();
            }
        }else{
            yCenter = clampToLimits ? Math.min(255, config.getInt("spawn zone.y center")) : config.getInt("spawn zone.y center");
        }

        //Multiverse, essentials and vanilla spawnpoints.
        if(config.getInt("spawn zone.z center") == -1) {
            if(config.getBoolean("spawn zone.default to multiverse")){
                //TODO: Multiverse integration.
                zCenter = 0;
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


    private void defineForbiddenRegion() {
        FileConfiguration config = plugin.getConfig();

        boolean enabled = config.getBoolean("no spawn zone.enabled");

        int xCenter = enabled ? config.getInt("no spawn zone.x center"): 0;
        int zCenter = enabled ? config.getInt("no spawn zone.z center"): 0;

        int xRange = enabled ? config.getInt("no spawn zone.x range") : 0;
        int zRange = enabled ? config.getInt("no spawn zone.z range") : 0;

        this.forbiddenRegion = Region.newRegionByRanges(xCenter, 0, zCenter,
                xRange, 0, zRange);
    }




    //FIELDS

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

    public boolean isNonWhiteListSafe(){
        return nonWhiteListSafe;
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

    public String getPrefix(){
        return prefix;
    }

    public String getWorldName(){
        return worldName;
    }

    public String getFirstJoinHomeName(){
        return firstJoinHomeName;
    }

    public List<String> getBlockBlackList(){
        return blockBlackList;
    }

    public List<String> getBlockWhiteList(){
        return blockWhiteList;
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




}
