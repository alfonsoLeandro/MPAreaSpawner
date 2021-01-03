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
    private boolean replaceUsedLocation;
    private boolean cacheEnabled;
    private boolean topToBottom;
    private boolean nonWhiteListSafe;
    private boolean checkPastSurface;
    private int cachedLocationsAmount;
    private int findSafeLocationAttempts;
    private int airGapAbove;
    private int timeBetweenLocations;
    private String prefix;
    private String worldName;
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
        this.replaceUsedLocation = config.getBoolean("delete location on use");
        this.cacheEnabled = config.getBoolean("enable cache");
        this.topToBottom = config.getBoolean("top to bottom");
        this.nonWhiteListSafe = config.getBoolean("non-whitelist are safe");
        this.checkPastSurface = config.getBoolean("check past surface");

        this.findSafeLocationAttempts = config.getInt("safe spawn attempts");
        this.cachedLocationsAmount = config.getInt("amount of cached spawns");
        this.airGapAbove = config.getInt("air gap above");
        this.timeBetweenLocations = config.getInt("time between generating locations");

        this.prefix = config.getString("prefix");
        this.worldName = config.getString("spawn world");

        this.blockBlackList = config.getStringList("block blacklist");
        this.blockWhiteList = config.getStringList("block whitelist");

        this.world = Bukkit.getWorld(worldName);

        defineAllowedRegion();
        defineForbiddenRegion();
    }

    private void defineAllowedRegion(){
        //TODO: essentials & multiverse options.
        FileConfiguration config = plugin.getConfig();

        boolean clampToLimits = config.getBoolean("spawn zone.clamp to limits");

        int xCenter = clampToLimits ? Math.min(29_999_984, config.getInt("spawn zone.x center")) : config.getInt("spawn zone.x center");
        int yCenter = clampToLimits ? Math.min(256, config.getInt("spawn zone.y center")) : config.getInt("spawn zone.y center");
        int zCenter = clampToLimits ? Math.min(29_999_984, config.getInt("spawn zone.z center")) : config.getInt("spawn zone.z center");

        int xRange = clampToLimits ? Math.min(29_999_984, config.getInt("spawn zone.x range")) : config.getInt("spawn zone.x range");
        int yRange = clampToLimits ? Math.min(256, config.getInt("spawn zone.y range")) : config.getInt("spawn zone.y range");
        int zRange = clampToLimits ? Math.min(29_999_984, config.getInt("spawn zone.z range")) : config.getInt("spawn zone.z range");

        if(clampToLimits){
            if(xCenter+xRange > 29_999_984 || xCenter-xRange < 29_999_984) xRange = 29_999_984 - xCenter;
            if(yCenter+yRange > 256 || yCenter-yRange < 0) yRange = 128 - yCenter;
            if(zCenter+zRange > 29_999_984 || zCenter-zRange < 29_999_984) zRange = 29_999_984 - zCenter;
        }


        this.allowedRegion = Region.newRegionByRanges(xCenter, yCenter, zCenter,
                xRange, yRange, zRange);
    }


    private void defineForbiddenRegion() {
        //TODO: essentials & multiverse options.
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

    public boolean isReplaceUsedLocation(){
        return replaceUsedLocation;
    }

    public boolean isCacheEnabled(){
        return cacheEnabled;
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
